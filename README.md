AI Credit Risk Evaluator
By: Aayush Jain

A full-stack application that combines deterministic credit risk scoring with LLM-powered explanations.

## Overview

This project demonstrates a hybrid architecture where:

- A rule-based engine computes credit risk decisions
- An LLM (OpenAI) generates natural language explanations and actionable suggestions

## Key Features

- Deterministic credit risk scoring (income, credit score, employment)
- Structured decision output (APPROVE / REVIEW / REJECT)
- LLM-powered explanations grounded in system logic
- Actionable recommendations for improving creditworthiness
- Clean React UI for interactive evaluation

## Architecture

Frontend (React)
→ FastAPI (scoring + LLM Layer)
→ OpenAI API (Explanation + Suggestions)

## Setup & Run (Full System)

Follow these steps in order to run the complete application locally.

---

### 1. Clone the Repository

```bash
git clone <your-repo-url>
cd CreditScore
```

---

## 2. Start Backend Services

### 2.1 FastAPI (LLM Explanation Layer)

```bash
cd ai-service

# Create virtual environment (first time only)
python -m venv venv

# Activate virtual environment
# Windows:
venv\Scripts\activate

# Mac/Linux:
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run FastAPI server
uvicorn main:app --reload
```

FastAPI runs at:

```
http://localhost:8000
```

Swagger docs (for testing):

```
http://localhost:8000/docs
```

---

### 2.2 Spring Boot (Credit Evaluation Engine)

Open a new terminal:

```bash
cd backend/demo

# Build project
./mvnw clean install

# Run Spring Boot server
./mvnw spring-boot:run
```

Spring Boot runs at:

```
http://localhost:8080
```

---

## 2.3 Test with Curl Command (Optional)

Open a new terminal:

````bash
cd backend/demo

#Run command (replace with your input values)
curl -X POST http://localhost:8080/api/evaluate -H "Content-Type: application/json" -d '{
  "income": 50000,
  "creditScore": 720,
  "employmentStatus": "employed"
}' | python -m json.tool

---
## 3. Start Frontend (React UI)

Open a new terminal:

```bash
cd frontend

npm install
npm run dev
````

Frontend runs at:

```
http://localhost:5173
```

---

## 4. Run the Application

1. Open the frontend in your browser
2. Enter applicant details (income, credit score, employment status)
3. Submit the form
4. View:
   - Credit decision (from Spring Boot)
   - Explanation and suggestions (from FastAPI + LLM)

---

## Important Notes

- Start **FastAPI before Spring Boot** (Spring depends on it)
- Ensure your `.env` file is set up in `ai-service` with your OpenAI API key
- Do NOT commit `.env` to version control
- Keep all three services running simultaneously during usage

## WHY THIS MATTERS
Traditional credit systems lack transparency. This system introduces interpretable decision-making through
structured logic and natural language explanations

## SECURITY
API keys are stored locally in environment variables and aren't included in repo. Template file is given for guidance

## Future Improvements

1. What-If simulation
2. ML based scoring
3. User session Tracking
4. Cloud Deployment

## Sample Test Cases

Use the following inputs to test different decision outcomes in the system.

---

### APPROVE — Strong Applicants

| Case          | Income | Credit Score | Employment Status |
| ------------- | ------ | ------------ | ----------------- |
| Case 1        | 95,000 | 780          | EMPLOYED          |
| Case 2        | 85,000 | 760          | EMPLOYED          |
| Case 3 (Edge) | 80,000 | 750          | EMPLOYED          |

---

### REVIEW — Borderline / Mixed Risk (Most Important)

| Case                 | Income | Credit Score | Employment Status |
| -------------------- | ------ | ------------ | ----------------- |
| Case 1 (Balanced)    | 80,000 | 750          | EMPLOYED          |
| Case 2 (Income Weak) | 65,000 | 680          | SELF_EMPLOYED     |
| Case 3 (Credit Weak) | 70,000 | 660          | EMPLOYED          |

---

### REJECT — High Risk Applicants

| Case                              | Income | Credit Score | Employment Status |
| --------------------------------- | ------ | ------------ | ----------------- |
| Case 1 (Clear Fail)               | 70,000 | 660          | EMPLOYED          |
| Case 2 (Credit Failure)           | 40,000 | 580          | UNEMPLOYED        |
| Case 3 (Income + Employment Risk) | 45,000 | 640          | UNEMPLOYED        |

---

### Notes

* Employment status values should be:

  * `EMPLOYED`
  * `UNEMPLOYED`
  * `SELF_EMPLOYED`
* These cases are designed to demonstrate:

  * deterministic scoring behavior
  * LLM-generated explanations across risk tiers

