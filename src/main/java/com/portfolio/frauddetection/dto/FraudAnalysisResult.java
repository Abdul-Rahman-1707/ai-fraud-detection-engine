package com.portfolio.frauddetection.dto;

import com.portfolio.frauddetection.model.RiskLevel;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudAnalysisResult {

    private String transactionId;
    private double riskScore;
    private RiskLevel riskLevel;
    private boolean isFraudulent;
    private List<String> ruleViolations;
    private String aiAnalysis;
    private long processingTimeMs;
}
