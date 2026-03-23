from fastapi import FastAPI, Request
from pydantic import BaseModel, validator
from slowapi import Limiter
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware

import os
import json
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))
# test to verify key works (before full test)
if not os.getenv("OPENAI_API_KEY"):
    raise ValueError("Missing OpenAI API Key")

# Creating the app using FastAPI - making a web server framework ie get HTTP requests
app = FastAPI()
print("MAIN.PY loaded")

# ---------------------------
# CORS (for React frontend)
# ---------------------------
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ---------------------------
# Rate Limiter Setup
# ---------------------------
limiter = Limiter(key_func=get_remote_address)
app.state.limiter = limiter


@app.exception_handler(RateLimitExceeded)
def rate_limit_handler(request: Request, exc: RateLimitExceeded):
    return JSONResponse(
        status_code=429,
        content={"error": "Rate limit exceeded. Try again later."},
    )


# Define input schema (validate incoming data, enforce structure, and converts JSON -> Python object)
class Applicant(
    BaseModel
):  # takes json input and makes it python (parsing + validation)
    income: int  # this becomes applicant.income
    creditScore: int  # this becomes applicant.creditScore
    employmentStatus: str  # this becomes applicant.employeeStatus

    # STANDARDIZE EMPLOYMENT INPUT
    @validator("employmentStatus")
    def normalize_employment(cls, value):
        val = value.lower().strip()

        mapping = {
            "employed": "employed",
            "full-time": "employed",
            "full_time": "employed",
            "self-employed": "self_employed",
            "self employed": "self_employed",
            "contractor": "self_employed",
            "unemployed": "unemployed",
            "none": "unemployed",
        }

        return mapping.get(val, "unemployed")


# ---------------------------
# GPT Explanation Layer
# ---------------------------
def generate_credit_explanation(data):
    print("GPT INPUT →", data)

    prompt = f"""
You are a financial risk analyst.

Given:
- Income: {data['income']}
- Credit Score: {data['credit_score']}
- Employment Status: {data['employment_status']}
- Risk Decision: {data['decision']}
- Key Risk Factors: {data['reasons']}

Return STRICT JSON in this format:
{{
    "explanation": "...",
    "suggestions": ["...", "...", "..."]
}}

Rules:
- Do NOT add extra text outside JSON
- Do NOT hallucinate missing factors
- Base reasoning ONLY on provided data
"""

    response = client.chat.completions.create(
        model="gpt-4.1-mini",
        messages=[
            {"role": "system", "content": "You are a precise financial assistant."},
            {"role": "user", "content": prompt},
        ],
        max_tokens=200,
        temperature=0.2,
    )

    raw_output = response.choices[0].message.content

    print("GPT RAW OUTPUT →", raw_output)

    # SAFE JSON PARSE (DO NOT TRUST MODEL BLINDLY)
    try:
        parsed = json.loads(raw_output)
        return parsed
    except:
        return {
            "explanation": "Unable to generate structured explanation.",
            "suggestions": [
                "Improve credit score",
                "Increase income stability",
                "Maintain consistent employment",
            ],
        }


# #Temporary Test Endpoint - Minimal Live test for GPT directly
@app.get("/test-gpt")
def test_gpt():
    response = client.chat.completions.create(
        model="gpt-4.1-mini",
        messages=[{"role": "user", "content": "Say hello"}],
        max_tokens=20,
    )

    return {"output": response.choices[0].message.content}


# defining the endpoint "analyze" ie exposing POST http://localhost:8000/analyze
@app.post("/analyze")
@limiter.limit("5/minute")  # 🔥 limiter applied here
def analyze(request: Request, applicant: Applicant):

    print("Received applicant:", applicant)

    score = 0
    reasons = []

    # convert score to business decision based on income, credit score, and employment status

    # credit score contribution (50%)
    if applicant.creditScore >= 750:
        score += 50
    elif applicant.creditScore >= 650:
        score += 40
        reasons.append("Credit Score is in the moderate range (650-749)")
    else:
        score += 20
        reasons.append("Credit Score is below preferred threshold (650)")

    # Income contribution (30%)
    if applicant.income >= 80000:
        score += 30
    elif applicant.income >= 60000:
        score += 20
        reasons.append("Income is in the moderate range (50000-79999)")
    else:
        score += 10
        reasons.append("Income is below preferred threshold (50000)")

    # Employment Contribution (20%)
    if applicant.employmentStatus == "employed":
        score += 20
    elif applicant.employmentStatus == "self_employed":
        score += 15
        reasons.append(
            "Employment status is self-employment, which carries moderate risk"
        )
    else:
        score += 5
        reasons.append("Employment status is high risk")

    # Get score
    print("Final Score: ", score)

    # Normalize riskScore
    riskScore = round((score / 100), 2)

    # Decision Logic
    if riskScore > 0.75:
        decision = "APPROVE"
    elif riskScore >= 0.5:
        decision = "REVIEW"
    else:
        decision = "REJECT"

    # Ensure APPROVE case has reasoning by avoiding empty explanations
    if not reasons:
        reasons.append("Strong performance across all evaluation factors")

    # GPT Explanation (structured JSON)
    gpt_output = generate_credit_explanation(
        {
            "income": applicant.income,
            "credit_score": applicant.creditScore,
            "employment_status": applicant.employmentStatus,
            "decision": decision,
            "reasons": reasons,
        }
    )

    response = {
        "decision": decision,
        "riskScore": riskScore,
        "reasons": reasons,
        "explanation": gpt_output.get("explanation"),
        "suggestions": gpt_output.get("suggestions"),
    }

    print("Returning response from FastAPI:", response)

    return response
