package com.portfolio.frauddetection.dto;

import com.portfolio.frauddetection.model.TransactionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {

    private String id;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCategory;
    private String country;
    private LocalDateTime timestamp;
    private TransactionStatus status;
    private Double fraudScore;
    private String fraudReason;
}
