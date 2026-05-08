package com.portfolio.frauddetection.dto;

import com.portfolio.frauddetection.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionRequest {

    @NotBlank
    private String userId;

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    private String merchantName;

    @NotBlank
    private String merchantCategory;

    private String cardLast4;

    @NotBlank
    private String country;

    private String city;

    private String ipAddress;

    private String deviceId;

    private TransactionType type;
}
