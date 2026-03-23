package com.example.demo.controller;

import com.example.demo.model.Applicant;
import com.example.demo.service.EligibilityService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
// import java.util.HashMap;
// import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class EligibilityController {

    private final EligibilityService service;

    public EligibilityController(EligibilityService service) {
        this.service = service;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<?> checkEligibility(@Valid @RequestBody Applicant applicant) {
        return ResponseEntity.ok(service.evaluate(applicant));
    }
    // boolean isEligible = evaluateEligibility(applicant);

    // Map<String, Object> response = new HashMap<>();
    // response.put("eligible", isEligible);
    // response.put("applicant", applicant);

    // return ResponseEntity.ok(response);
}

// private boolean evaluateEligibility(Applicant applicant) {
// return applicant.getIncome() >= 30000
// && applicant.getCreditScore() >= 650
// && applicant.getEmploymentStatus() != null;
// }

/*
 * public Map<String, Object> evaluate(@Valid @RequestBody Applicant applicant)
 * {
 * 
 * logger.
 * info("Received request: income={}, creditScore={}, employmentStatus={}",
 * applicant.getIncome(),
 * applicant.getCreditScore(),
 * applicant.getEmploymentStatus());
 * 
 * Map<String, Object> response = service.evaluate(applicant);
 * 
 * logger.info("Returning response: {}", response);
 * logger.info("Controller hit BEFORE validation");
 * 
 * return response;
 * }
 */