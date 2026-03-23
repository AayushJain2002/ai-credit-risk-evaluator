package com.example.demo.service;

import com.example.demo.model.Applicant;
import com.example.demo.model.EligibilityResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EligibilityService {

    private static final Logger logger = LoggerFactory.getLogger(EligibilityService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    public EligibilityResponse evaluate(Applicant applicant) {

        String url = "http://localhost:8000/analyze";

        logger.info("Calling FastAPI at {}", url);
        logger.info("Payload sent to FastAPI: {}", applicant);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Applicant> request = new HttpEntity<>(applicant, headers);

        ResponseEntity<EligibilityResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        request,
                        new ParameterizedTypeReference<EligibilityResponse>() {}
                );

        logger.info("Response received from FastAPI: {}", response.getBody());

        return response.getBody();
    }
}