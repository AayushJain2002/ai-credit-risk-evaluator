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
        // Step 1 - Deterministic Scoring
        String decision;
        double riskScore;
        List<String> reasons = new ArrayList<>();
        if (applicant.getCreditScore() >= 700 && applicant.getIncome() >= 50000) {
            decision = "APPROVE";
            riskScore = 2;
            reasons.add("Strong credit score");
            reasons.add("Stable income");
        } else if (applicant.getCreditScore() >= 600) {
            decision = "REVIEW";
            riskScore = 5;
            reasons.add("Moderate credit score");
        } else {
            decision = "REJECT";
            riskScore = 8;
            reasons.add("Low credit score");
        }
        // Step 2 - Prepare payload for FastAPI
        Map<String, Object> payload = new HashMap<>();
        payload.put("decision", decision);
        payload.put("riskScore", riskScore);
        payload.put("reasons", reasons);

        String url = "http://localhost:8000/analyze";

        logger.info("Calling FastAPI at {}", url);
        logger.info("Payload sent to FastAPI: {}", applicant);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Applicant> request = new HttpEntity<>(applicant, headers);

        // Step 3 - Call FastAPI
        ResponseEntity<EligibilityResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<EligibilityResponse>() {
                });
        // logger.info("Response received from FastAPI: {}", response.getBody());
        EligibilityResponse responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("FastAPI returned null response");
        }

        // Step 4 - Merge deterministic + AI output
        responseBody.setDecision(decision);
        responseBody.setRiskScore(riskScore);
        responseBody.setReasons(reasons);
        
        return responseBody;
    }
}