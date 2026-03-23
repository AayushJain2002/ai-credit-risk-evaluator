AI Credit Risk Evaluator
By: Aayush Jain

A full-stack application that combines deterministic credit risk scoring with LLM-powered explainability.

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

## Setup

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd CreditScore

### 2. Configure your Environment

## 2a.1 Backend Setup
cd ai-serivce
python -m venv venv
source venv/Scripts/activate
pip install fastapi uvicorn slowapi openai python-dotenv (or make a requirements.txt and install from there via pip install -r requirements.txt)


## 2a.2 create .env file
Make a .env file inside ai-service folder and add your API key locally. DO NOT commit this file to version control.
OPEN_API_KEY = your_api_key_here

#run backend
python -m uvicorn main:app --reload


## 2b. Frontend Setup
cd frontend
npm install
npm run dev
#front end runs at
http://localhost:5713

#WHY THIS MATTERS
Traditional credit systems lack transparency. This system introduces interpretable decision-making through 
structured logic and natural language explanations

#SECURITY
API keys are stored locally in environment variables and aren't included in repo. Template file is given for guidance

#Future Improvements
1. What-If simulation
2. ML based scoring
3. User session Tracking
4. Cloud Deployment

#Sample cases to run
Approval - Case 1 - Strong
Income: 95000
Credit Score: 780
Employment: employed

Approval - Case 2 - Relatively Strong
Income: 85000
Credit Score: 760
Employment: employed

Approval - Case 3 - edge case 
Income: 80000
Credit Score: 750
Employment: employed

Review Cases [MOST IMPORTANT]
Case 1 (balanced)
Income: 80000
Credit Score: 750
Employment: employed

Case 2 (credit ok, income weak)
Income: 65000
Credit Score: 680
Employment: self-employed

Case 3 (income ok, credit weaker)
Income: 70000
Credit Score: 660
Employment: employed

Reject cases (CLEAR FAIL)
Case 1 (Obvious)
Income: 70000
Credit Score: 660
Employment: employed

Case 2 (Credit Failure)
Income: 40000
Credit Score: 580
Employment: unemployed

Case 3 (income + employment risk)
Income: 45000
Credit Score: 640
Employment: unemployed