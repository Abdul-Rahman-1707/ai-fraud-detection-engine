package com.portfolio.frauddetection.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardStats {

    private long totalTransactions;
    private long flaggedTransactions;
    private long blockedTransactions;
    private long approvedTransactions;
    private BigDecimal totalAmountProcessed;
    private BigDecimal totalAmountBlocked;
    private double avgFraudScore;
    private long openAlerts;
}
