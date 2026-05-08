package com.portfolio.frauddetection.dto;

import com.portfolio.frauddetection.model.AlertStatus;
import com.portfolio.frauddetection.model.RiskLevel;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertResponse {

    private String id;
    private String transactionId;
    private String userId;
    private Double riskScore;
    private RiskLevel riskLevel;
    private String ruleViolations;
    private String aiAnalysis;
    private AlertStatus alertStatus;
    private LocalDateTime createdAt;
}
