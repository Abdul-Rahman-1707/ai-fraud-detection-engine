package com.portfolio.frauddetection.service;

import com.portfolio.frauddetection.dto.FraudAnalysisResult;
import com.portfolio.frauddetection.dto.TransactionRequest;
import com.portfolio.frauddetection.dto.TransactionResponse;
import com.portfolio.frauddetection.model.*;
import com.portfolio.frauddetection.repository.FraudAlertRepository;
import com.portfolio.frauddetection.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private FraudAlertRepository alertRepository;
    @Mock private FraudDetectionService fraudDetectionService;
    @InjectMocks private TransactionService transactionService;

    private Transaction sampleTransaction;
    private TransactionRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleTransaction = Transaction.builder()
                .id("txn-001")
                .userId("user-123")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .merchantName("Amazon")
                .merchantCategory("RETAIL")
                .country("US")
                .city("New York")
                .cardLast4("4242")
                .timestamp(LocalDateTime.now())
                .status(TransactionStatus.APPROVED)
                .fraudScore(0.15)
                .createdAt(LocalDateTime.now())
                .build();

        sampleRequest = TransactionRequest.builder()
                .userId("user-123")
                .amount(new BigDecimal("150.00"))
                .currency("USD")
                .merchantName("Amazon")
                .merchantCategory("RETAIL")
                .country("US")
                .city("New York")
                .cardLast4("4242")
                .build();
    }

    @Test
    void processTransaction_shouldSaveAndAnalyze() {
        when(transactionRepository.save(any(Transaction.class))).thenReturn(sampleTransaction);
        when(transactionRepository.findById("txn-001")).thenReturn(Optional.of(sampleTransaction));
        when(fraudDetectionService.analyzeTransaction(any()))
                .thenReturn(FraudAnalysisResult.builder()
                        .transactionId("txn-001")
                        .riskScore(0.15)
                        .riskLevel(RiskLevel.LOW)
                        .isFraudulent(false)
                        .ruleViolations(List.of())
                        .processingTimeMs(50)
                        .build());

        TransactionResponse response = transactionService.processTransaction(sampleRequest);

        assertThat(response.getId()).isEqualTo("txn-001");
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(fraudDetectionService, times(1)).analyzeTransaction(any());
    }

    @Test
    void getTransaction_shouldReturnTransaction() {
        when(transactionRepository.findById("txn-001")).thenReturn(Optional.of(sampleTransaction));

        TransactionResponse response = transactionService.getTransaction("txn-001");

        assertThat(response.getId()).isEqualTo("txn-001");
        assertThat(response.getUserId()).isEqualTo("user-123");
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void getTransaction_notFound_shouldThrow() {
        when(transactionRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction("invalid"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getUserTransactions_shouldReturnList() {
        when(transactionRepository.findByUserIdOrderByTimestampDesc("user-123"))
                .thenReturn(List.of(sampleTransaction));

        List<TransactionResponse> results = transactionService.getUserTransactions("user-123");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo("user-123");
    }
}
