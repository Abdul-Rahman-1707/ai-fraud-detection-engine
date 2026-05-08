package com.portfolio.frauddetection.engine;

import com.portfolio.frauddetection.model.Transaction;
import com.portfolio.frauddetection.model.TransactionStatus;
import com.portfolio.frauddetection.model.UserProfile;
import com.portfolio.frauddetection.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleBasedFraudDetectorTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private RuleBasedFraudDetector detector;

    private UserProfile profile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(detector, "velocityWindowMinutes", 10);
        ReflectionTestUtils.setField(detector, "maxTransactionsPerWindow", 5);
        ReflectionTestUtils.setField(detector, "highAmountThreshold", new BigDecimal("5000.00"));

        profile = UserProfile.builder()
                .userId("user-123")
                .avgTransactionAmount(new BigDecimal("100.00"))
                .maxTransactionAmount(new BigDecimal("500.00"))
                .primaryCountry("US")
                .knownDeviceIds("device-001,device-002")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void evaluate_normalTransaction_shouldReturnLowScore() {
        Transaction txn = buildTransaction("50.00", "US", "device-001", 14);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(1L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.score()).isLessThan(0.3);
        assertThat(result.violations()).isEmpty();
    }

    @Test
    void evaluate_highAmount_shouldFlag() {
        Transaction txn = buildTransaction("10000.00", "US", "device-001", 14);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(0L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.score()).isGreaterThanOrEqualTo(0.2);
        assertThat(result.violations()).anyMatch(v -> v.contains("HIGH_AMOUNT"));
    }

    @Test
    void evaluate_velocityBreach_shouldFlag() {
        Transaction txn = buildTransaction("100.00", "US", "device-001", 14);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(6L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.score()).isGreaterThanOrEqualTo(0.3);
        assertThat(result.violations()).anyMatch(v -> v.contains("VELOCITY_BREACH"));
    }

    @Test
    void evaluate_foreignCountry_shouldFlag() {
        Transaction txn = buildTransaction("100.00", "NG", "device-001", 14);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(0L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.violations()).anyMatch(v -> v.contains("GEO_ANOMALY"));
    }

    @Test
    void evaluate_unknownDevice_shouldFlag() {
        Transaction txn = buildTransaction("100.00", "US", "unknown-device", 14);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(0L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.violations()).anyMatch(v -> v.contains("UNKNOWN_DEVICE"));
    }

    @Test
    void evaluate_oddHours_shouldFlag() {
        Transaction txn = buildTransaction("100.00", "US", "device-001", 3);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(0L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.violations()).anyMatch(v -> v.contains("ODD_HOURS"));
    }

    @Test
    void evaluate_multipleViolations_shouldStack() {
        Transaction txn = buildTransaction("8000.00", "NG", "unknown-device", 3);
        when(transactionRepository.countRecentTransactions(eq("user-123"), any())).thenReturn(7L);

        var result = detector.evaluate(txn, profile);

        assertThat(result.score()).isGreaterThanOrEqualTo(0.5);
        assertThat(result.violations().size()).isGreaterThanOrEqualTo(3);
    }

    private Transaction buildTransaction(String amount, String country, String deviceId, int hour) {
        return Transaction.builder()
                .id("txn-test")
                .userId("user-123")
                .amount(new BigDecimal(amount))
                .currency("USD")
                .merchantName("Test Merchant")
                .merchantCategory("RETAIL")
                .country(country)
                .deviceId(deviceId)
                .timestamp(LocalDateTime.now().withHour(hour))
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
