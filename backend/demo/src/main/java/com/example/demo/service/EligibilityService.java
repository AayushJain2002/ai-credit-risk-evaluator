package com.example.demo.service;

import com.example.demo.model.Applicant;
import com.example.demo.model.EligibilityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class EligibilityService {

    private static final Logger logger = LoggerFactory.getLogger(EligibilityService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public EligibilityResponse evaluate(Applicant applicant) {

        // -------------------------------
        // Step 0 - HARD REJECTION RULES
        // -------------------------------
        List<String> reasons = new ArrayList<>();

        // Absolute failures
        if (applicant.getCreditScore() < 600) {
            reasons.add("Credit score is critically low (<600)");
            return buildReject(reasons);
        }

        if (applicant.getEmploymentStatus().equalsIgnoreCase("UNEMPLOYED")
                && applicant.getIncome() < 60000) {
            reasons.add("Unemployed with insufficient income stability");
            return buildReject(reasons);
        }

        // Combination risk (IMPORTANT FIX)
        if (applicant.getCreditScore() < 670
                && applicant.getEmploymentStatus().equalsIgnoreCase("SELF_EMPLOYED")) {
            reasons.add("Low credit combined with self-employment creates elevated risk");
            return buildReject(reasons);
        }

        // -------------------------------
        // Step 1 - Deterministic Scoring
        // -------------------------------
        double score = 0;

        // CREDIT (50 max)
        if (applicant.getCreditScore() >= 750) {
            score += 50;
        } else if (applicant.getCreditScore() >= 700) {
            score += 40;
            reasons.add("Credit Score is in the moderate range (700-749)");
        } else if (applicant.getCreditScore() >= 650) {
            score += 30;
            reasons.add("Credit Score is below preferred threshold (650-699)");
        } else {
            score += 20;
            reasons.add("Credit Score is low (<650)");
        }

        // INCOME (30 max)
        if (applicant.getIncome() >= 90000) {
            score += 30;
        } else if (applicant.getIncome() >= 75000) {
            score += 27;
        } else if (applicant.getIncome() >= 60000) {
            score += 22;
            reasons.add("Income is below preferred threshold (60000-74999)");
        } else {
            score += 12; // harsher penalty
            reasons.add("Income is low (<60000)");
        }

        // EMPLOYMENT (20 max)
        if (applicant.getEmploymentStatus().equalsIgnoreCase("EMPLOYED")) {
            score += 20;
        } else if (applicant.getEmploymentStatus().equalsIgnoreCase("SELF_EMPLOYED")) {
            score += 10; // harsher penalty
            reasons.add("Self-employment introduces moderate risk");
        } else {
            score += 0; // unemployed = no points
            reasons.add("Unemployment is high risk");
        }

        // Normalize score
        double riskScore = Math.round(score * 100.0) / 100.0;

        // -------------------------------
        // Step 2 - Decision Logic
        // -------------------------------
        String decision;
        if (riskScore >= 85) {
            decision = "APPROVE";
        } else if (riskScore >= 65) {
            decision = "REVIEW";
        } else {
            decision = "REJECT";
        }

        if (reasons.isEmpty()) {
            reasons.add("Strong performance across all evaluation factors");
        }

        // -------------------------------
        // Step 3 - Prepare payload for FastAPI
        // -------------------------------
        Map<String, Object> payload = new HashMap<>();
        payload.put("income", applicant.getIncome());
        payload.put("credit_score", applicant.getCreditScore());
        payload.put("employment_status", applicant.getEmploymentStatus().toLowerCase());
        payload.put("decision", decision);
        payload.put("riskScore", riskScore);
        payload.put("reasons", reasons);

        String url = "http://127.0.0.1:8000/analyze";

        logger.info("Calling FastAPI at {}", url);
        logger.info("Payload sent to FastAPI: {}", payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        // -------------------------------
        // Step 4 - Call FastAPI
        // -------------------------------
        ResponseEntity<EligibilityResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<EligibilityResponse>() {
                });

        EligibilityResponse responseBody = response.getBody();

        if (responseBody == null) {
            throw new RuntimeException("FastAPI returned null response");
        }

        // -------------------------------
        // Step 5 - Merge deterministic + AI output
        // -------------------------------
        responseBody.setDecision(decision);
        responseBody.setRiskScore(riskScore);
        responseBody.setReasons(reasons);

        return responseBody;
    }

    private EligibilityResponse buildReject(List<String> reasons) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("income", 0);
        payload.put("credit_score", 0);
        payload.put("employment_status", "unknown");
        payload.put("decision", "REJECT");
        payload.put("riskScore", 50.0);
        payload.put("reasons", reasons);

        String url = "http://127.0.0.1:8000/analyze";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<EligibilityResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<EligibilityResponse>() {
                });

        EligibilityResponse responseBody = response.getBody();

        if (responseBody == null) {
            throw new RuntimeException("FastAPI returned null response");
        }

        responseBody.setDecision("REJECT");
        responseBody.setRiskScore(50.0);
        responseBody.setReasons(reasons);

        return responseBody;
    }
}