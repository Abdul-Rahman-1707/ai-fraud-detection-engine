package com.portfolio.frauddetection.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionSummary {

    private LocalDate date;
    private long totalCount;
    private long approvedCount;
    private long blockedCount;
    private long flaggedCount;
    private BigDecimal totalAmount;
    private BigDecimal blockedAmount;
    private double avgFraudScore;
    private double maxFraudScore;

    public double getBlockRate() {
        return totalCount > 0 ? (double) blockedCount / totalCount * 100 : 0.0;
    }

    public double getFlagRate() {
        return totalCount > 0 ? (double) flaggedCount / totalCount * 100 : 0.0;
    }
}
