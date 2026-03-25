from fastapi import FastAPI, Request
from pydantic import BaseModel
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
    allow_origins=["*"],  # safe for demo, restrict in production
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


# ---------------------------
# Request Schema (RESTORES SWAGGER BODY)
# ---------------------------
class AnalyzeRequest(BaseModel):
    income: int
    credit_score: int
    employment_status: str
    decision: str
    riskScore: float
    reasons: list[str]


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


# ---------------------------
# Root Endpoint
# ---------------------------
@app.get("/")
def root():
    return {"message": "FastAPI service is running"}


# ---------------------------
# Temporary Test Endpoint
# ---------------------------
@app.get("/test-gpt")
def test_gpt():
    response = client.chat.completions.create(
        model="gpt-4.1-mini",
        messages=[{"role": "user", "content": "Say hello"}],
        max_tokens=20,
    )

    return {"output": response.choices[0].message.content}


# ---------------------------
# Analyze Endpoint (LLM ONLY — NO SCORING)
# ---------------------------
@app.post("/analyze")
@limiter.limit("5/minute")
async def analyze(request: Request, payload: AnalyzeRequest):

    # Convert Pydantic model → dict (Pydantic v2 safe)
    data = payload.model_dump()

    print("RAW DATA RECEIVED FROM SPRING:", data)

    # GPT Explanation (NO SCORING HERE)
    gpt_output = generate_credit_explanation(data)

    response = {
        "decision": data["decision"],
        "riskScore": data["riskScore"],
        "reasons": data["reasons"],
        "explanation": gpt_output.get("explanation"),
        "suggestions": gpt_output.get("suggestions"),
    }

    print("Returning response from FastAPI:", response)

    return response
