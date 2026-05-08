package com.portfolio.frauddetection.engine;

import com.portfolio.frauddetection.model.Transaction;
import com.portfolio.frauddetection.model.UserProfile;
import com.portfolio.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleBasedFraudDetector {

    private final TransactionRepository transactionRepository;

    @Value("${app.fraud.velocity-window-minutes}")
    private int velocityWindowMinutes;

    @Value("${app.fraud.max-transactions-per-window}")
    private int maxTransactionsPerWindow;

    @Value("${app.fraud.high-amount-threshold}")
    private BigDecimal highAmountThreshold;

    public FraudRuleResult evaluate(Transaction transaction, UserProfile profile) {
        List<String> violations = new ArrayList<>();
        double score = 0.0;

        score += checkHighAmount(transaction, profile, violations);
        score += checkVelocity(transaction, violations);
        score += checkGeographicAnomaly(transaction, profile, violations);
        score += checkDeviceAnomaly(transaction, profile, violations);
        score += checkTimeAnomaly(transaction, violations);
        score += checkAmountDeviation(transaction, profile, violations);

        score = Math.min(score, 1.0);

        log.info("Rule evaluation for txn {}: score={}, violations={}",
                transaction.getId(), score, violations);

        return new FraudRuleResult(score, violations);
    }

    private double checkHighAmount(Transaction txn, UserProfile profile, List<String> violations) {
        if (txn.getAmount().compareTo(highAmountThreshold) > 0) {
            violations.add("HIGH_AMOUNT: Transaction amount $" + txn.getAmount() + " exceeds threshold $" + highAmountThreshold);
            return 0.2;
        }
        if (profile != null && profile.getMaxTransactionAmount() != null) {
            if (txn.getAmount().compareTo(profile.getMaxTransactionAmount().multiply(BigDecimal.valueOf(2))) > 0) {
                violations.add("UNUSUAL_AMOUNT: Amount is 2x higher than user's historical max");
                return 0.25;
            }
        }
        return 0.0;
    }

    private double checkVelocity(Transaction txn, List<String> violations) {
        LocalDateTime windowStart = txn.getTimestamp().minusMinutes(velocityWindowMinutes);
        long recentCount = transactionRepository.countRecentTransactions(txn.getUserId(), windowStart);

        if (recentCount >= maxTransactionsPerWindow) {
            violations.add("VELOCITY_BREACH: " + recentCount + " transactions in last " + velocityWindowMinutes + " minutes");
            return 0.3;
        }
        return 0.0;
    }

    private double checkGeographicAnomaly(Transaction txn, UserProfile profile, List<String> violations) {
        if (profile == null || profile.getPrimaryCountry() == null) return 0.0;

        if (!txn.getCountry().equalsIgnoreCase(profile.getPrimaryCountry())) {
            violations.add("GEO_ANOMALY: Transaction from " + txn.getCountry() + ", user's primary country is " + profile.getPrimaryCountry());
            return 0.25;
        }
        return 0.0;
    }

    private double checkDeviceAnomaly(Transaction txn, UserProfile profile, List<String> violations) {
        if (profile == null || profile.getKnownDeviceIds() == null || txn.getDeviceId() == null) return 0.0;

        if (!profile.getKnownDeviceIds().contains(txn.getDeviceId())) {
            violations.add("UNKNOWN_DEVICE: Device " + txn.getDeviceId() + " not in user's known devices");
            return 0.15;
        }
        return 0.0;
    }

    private double checkTimeAnomaly(Transaction txn, List<String> violations) {
        int hour = txn.getTimestamp().getHour();
        if (hour >= 1 && hour <= 5) {
            violations.add("ODD_HOURS: Transaction at " + hour + ":00 (unusual activity window)");
            return 0.1;
        }
        return 0.0;
    }

    private double checkAmountDeviation(Transaction txn, UserProfile profile, List<String> violations) {
        if (profile == null || profile.getAvgTransactionAmount() == null) return 0.0;

        BigDecimal avg = profile.getAvgTransactionAmount();
        if (avg.compareTo(BigDecimal.ZERO) > 0) {
            double ratio = txn.getAmount().doubleValue() / avg.doubleValue();
            if (ratio > 5.0) {
                violations.add("AMOUNT_DEVIATION: Transaction is " + String.format("%.1f", ratio) + "x the user's average");
                return 0.2;
            }
        }
        return 0.0;
    }

    public record FraudRuleResult(double score, List<String> violations) {}
}
