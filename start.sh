#!/usr/bin/env bash

set -e  # Exit immediately if a command fails

echo "Starting AI Credit Risk Evaluator..."

# ---------- FASTAPI ----------
echo "Starting FastAPI service..."

cd ai-service

# Activate virtual environment
if [ -f "venv/Scripts/activate" ]; then
    source venv/Scripts/activate   # Windows Git Bash
elif [ -f "venv/bin/activate" ]; then
    source venv/bin/activate       # Mac/Linux
else
    echo "ERROR: Virtual environment not found."
    exit 1
fi

# Install dependencies (safe to run multiple times)
pip install -r requirements.txt > /dev/null 2>&1

# Start FastAPI in background
uvicorn main:app --reload &
FASTAPI_PID=$!

cd ..

# ---------- SPRING BOOT ----------
echo "Starting Spring Boot backend..."

cd backend/demo

# Ensure mvnw is executable
chmod +x mvnw

# Build and run in background
./mvnw clean install > /dev/null 2>&1
./mvnw spring-boot:run &
SPRING_PID=$!

cd ../..

# ---------- FRONTEND ----------
echo "Starting React frontend..."

cd frontend

# Install dependencies
npm install > /dev/null 2>&1

# Start frontend in background
npm run dev &
FRONTEND_PID=$!

cd ..

# ---------- FINAL ----------
echo ""
echo "All services started:"
echo "FastAPI → http://localhost:8000"
echo "Spring Boot → http://localhost:8080"
echo "Frontend → http://localhost:5173"
echo ""

echo "Press Ctrl+C to stop all services."

# Wait for processes
wait $FASTAPI_PID $SPRING_PID $FRONTEND_PID