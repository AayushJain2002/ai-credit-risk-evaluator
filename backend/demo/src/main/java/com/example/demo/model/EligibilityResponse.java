package com.example.demo.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "decision", "riskScore", "reasons", "explanation" })
public class EligibilityResponse {

    private String decision;
    private double riskScore;
    private String explanation;
    private List<String> reasons;

    public EligibilityResponse(String decision, double riskScore, String explanation) {
        this.decision = decision;
        this.riskScore = riskScore;
        this.explanation = explanation;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Override
    public String toString() {
        return "EligibilityResponse {" + '\n' +
                "decision='" + decision + '\n' +
                "riskScore=" + riskScore + '\n' +
                "reasons= " + reasons + '\n' +
                "explanation= " + explanation + '\'' +
                '}';
    }
}