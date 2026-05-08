package com.portfolio.frauddetection.service;

import com.portfolio.frauddetection.dto.AlertResponse;
import com.portfolio.frauddetection.model.AlertStatus;
import com.portfolio.frauddetection.model.FraudAlert;
import com.portfolio.frauddetection.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final FraudAlertRepository alertRepository;

    public Page<AlertResponse> getAlerts(AlertStatus status, Pageable pageable) {
        return alertRepository.findByAlertStatusOrderByCreatedAtDesc(status, pageable)
                .map(this::toResponse);
    }

    public List<AlertResponse> getUserAlerts(String userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AlertResponse updateAlertStatus(String alertId, AlertStatus newStatus, String reviewer) {
        FraudAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.setAlertStatus(newStatus);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        alertRepository.save(alert);

        return toResponse(alert);
    }

    private AlertResponse toResponse(FraudAlert a) {
        return AlertResponse.builder()
                .id(a.getId())
                .transactionId(a.getTransactionId())
                .userId(a.getUserId())
                .riskScore(a.getRiskScore())
                .riskLevel(a.getRiskLevel())
                .ruleViolations(a.getRuleViolations())
                .aiAnalysis(a.getAiAnalysis())
                .alertStatus(a.getAlertStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
