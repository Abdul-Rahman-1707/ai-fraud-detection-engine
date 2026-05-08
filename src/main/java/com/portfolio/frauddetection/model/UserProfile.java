package com.portfolio.frauddetection.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfile {

    @Id
    private String userId;

    private BigDecimal avgTransactionAmount;

    private BigDecimal maxTransactionAmount;

    private Integer avgDailyTransactionCount;

    private String primaryCountry;

    private String primaryCity;

    private String commonMerchantCategories;

    private String knownDeviceIds;

    private String knownIpAddresses;

    private LocalDateTime lastTransactionAt;

    private Long totalTransactions;

    private Long flaggedTransactions;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
