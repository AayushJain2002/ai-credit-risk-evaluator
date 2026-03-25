AI Credit Risk Evaluator
By: Aayush Jain

A full-stack application that combines deterministic credit risk scoring with LLM-powered explanations.

---

## Overview

This project demonstrates a hybrid architecture where:

* A rule-based engine computes credit risk decisions
* An LLM (OpenAI) generates natural language explanations and actionable suggestions

---

## Key Features

* Deterministic credit risk scoring (income, credit score, employment)
* Structured decision output (APPROVE / REVIEW / REJECT)
* LLM-powered explanations grounded in system logic
* Actionable recommendations for improving creditworthiness
* Clean React UI for interactive evaluation

---

## Architecture

Frontend (React)
→ Spring Boot (Deterministic Decision Engine)
→ FastAPI (LLM Explanation Layer)
→ OpenAI API

---

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

### 2.1 Spring Boot (Credit Evaluation Engine)

Open a new terminal:

```bash
cd backend/demo

./mvnw clean install
./mvnw spring-boot:run
```

Spring Boot runs at:

```
http://localhost:8080
```

---

### 2.2 FastAPI (LLM Explanation Layer)

```bash
cd ai-service

# Create virtual environment (first time only)
python -m venv venv

# Activate virtual environment

# Git Bash (recommended for Windows)
source venv/Scripts/activate

# Windows (PowerShell / CMD)
venv\Scripts\activate

# Mac/Linux
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt (first time only)

# Run FastAPI server
uvicorn main:app --reload
```

FastAPI runs at:

```
http://localhost:8000
```

Swagger docs:

```
http://localhost:8000/docs
```

---

## 2.3 Test with Curl (Optional)

```bash
cd backend/demo

curl -X POST http://localhost:8080/api/evaluate \
-H "Content-Type: application/json" \
-d '{
  "income": 50000,
  "creditScore": 720,
  "employmentStatus": "EMPLOYED"
}' | python -m json.tool
```

---

## 3. Start Frontend (React UI)

```bash
cd frontend

npm install
npm run dev
```

Frontend runs at:

```
http://localhost:5173
```

---

## 4. Run the Application

1. Open the frontend
2. Enter applicant details
3. Submit the form
4. View:

   * Credit decision (Spring Boot)
   * Explanation + suggestions (FastAPI + LLM)

---

## Quick Start (One Command)

```bash
./start.sh
```

This script:

* Activates the Python virtual environment
* Starts FastAPI
* Builds and runs Spring Boot
* Launches the React frontend

---

## System Design Philosophy

This system separates:

### Deterministic Layer (Spring Boot)

* Handles credit decision logic
* Ensures consistency and auditability

### AI Layer (FastAPI + LLM)

* Generates explanations and suggestions
* Does NOT control decisions

### Why This Matters

Most systems are either:

* Black-box AI (unreliable)
* Rigid rule engines (not interpretable)

This design combines both:

→ Deterministic accuracy + AI explainability

---

## Demo Focus

This project demonstrates:

* Multi-service architecture (Java + Python + React)
* Clean separation of logic vs AI
* Real-world applicability to lending and underwriting
* Production-style system design (not a toy ML model)

---

## Important Notes

* Start **FastAPI before Spring Boot**
* Ensure `.env` is configured in `ai-service`
* Do NOT commit `.env`
* Keep all services running simultaneously

---

## WHY THIS MATTERS

Traditional credit systems lack transparency.
This system introduces interpretable decision-making through structured logic + natural language explanations.

---

## SECURITY

API keys are stored locally in environment variables and are not included in the repository.

---

## Future Improvements

1. What-if simulation
2. ML-based scoring
3. User session tracking
4. Cloud deployment

---

## Sample Test Cases

### APPROVE — Strong Applicants

| Case          |  Income | Credit Score | Employment | Why
| ------------- | ------  | ------------ | ---------- | -----------------
| Elite         | 100,000 | 800          | EMPLOYED   | Max across all factors
| Stable        | 85,000  | 750          | EMPLOYED   | Strong but not max
| Edge          | 75,000  | 720          | EMPLOYED   | Barely crosses 80 threshold

---

### REVIEW — Mixed Risk

| Case        | Income | Credit Score | Employment    | Why
| ----------- | ------ | ------------ | ------------- | ------------
| Balanced    | 70,000 | 700          | EMPLOYED      | Right at lower-middle
| Income Weak | 55,000 | 730          | EMPLOYED      | Good credit, weak income
| Credit Weak | 80,000 | 720          | SELF_EMPLOYED | Strong profile dragged by employment

---

### REJECT — High Risk

| Case                     | Income | Credit Score |  Employment   | Why
| ------------------------ | ------ | ------------ | -----------   | -----------
| Clear Fail               | 50,000 | 580          | SELF_EMPLOYED | Just below threshold
| Credit Failure           | 50,000 | 650          | UNEMPLOYED    | Credit kills decision
| Income + Employment Risk | 70,000 | 640          | SELF_EMPLOYED | All three factors weak
---

### Notes

* Employment values must be:

  * `EMPLOYED`
  * `UNEMPLOYED`
  * `SELF_EMPLOYED`

* These cases demonstrate:

  * deterministic scoring behavior
  * LLM explanation differences across risk tiers
