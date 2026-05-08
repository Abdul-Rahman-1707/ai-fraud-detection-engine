package com.portfolio.frauddetection.service;

import com.portfolio.frauddetection.dto.FraudAnalysisResult;
import com.portfolio.frauddetection.engine.AiFraudAnalyzer;
import com.portfolio.frauddetection.engine.RuleBasedFraudDetector;
import com.portfolio.frauddetection.model.*;
import com.portfolio.frauddetection.repository.FraudAlertRepository;
import com.portfolio.frauddetection.repository.TransactionRepository;
import com.portfolio.frauddetection.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {

    private final RuleBasedFraudDetector ruleDetector;
    private final AiFraudAnalyzer aiAnalyzer;
    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository alertRepository;
    private final UserProfileRepository userProfileRepository;

    @Value("${app.fraud.score-threshold}")
    private double scoreThreshold;

    @Transactional
    public FraudAnalysisResult analyzeTransaction(Transaction transaction) {
        long startTime = System.currentTimeMillis();

        UserProfile profile = userProfileRepository.findById(transaction.getUserId()).orElse(null);

        var ruleResult = ruleDetector.evaluate(transaction, profile);

        var aiResult = aiAnalyzer.analyze(transaction, profile, ruleResult);

        double combinedScore = (ruleResult.score() * 0.4) + (aiResult.score() * 0.6);
        combinedScore = Math.min(combinedScore, 1.0);

        RiskLevel riskLevel = determineRiskLevel(combinedScore);
        boolean isFraudulent = combinedScore >= scoreThreshold;

        transaction.setFraudScore(combinedScore);
        transaction.setFraudReason(String.join("; ", ruleResult.violations()));
        transaction.setStatus(isFraudulent ? TransactionStatus.BLOCKED : TransactionStatus.APPROVED);
        transactionRepository.save(transaction);

        if (combinedScore >= 0.5) {
            createAlert(transaction, combinedScore, riskLevel, ruleResult, aiResult);
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("Fraud analysis complete for txn {}: score={}, risk={}, time={}ms",
                transaction.getId(), combinedScore, riskLevel, processingTime);

        return FraudAnalysisResult.builder()
                .transactionId(transaction.getId())
                .riskScore(combinedScore)
                .riskLevel(riskLevel)
                .isFraudulent(isFraudulent)
                .ruleViolations(ruleResult.violations())
                .aiAnalysis(aiResult.analysis())
                .processingTimeMs(processingTime)
                .build();
    }

    private RiskLevel determineRiskLevel(double score) {
        if (score >= 0.85) return RiskLevel.CRITICAL;
        if (score >= 0.65) return RiskLevel.HIGH;
        if (score >= 0.4) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private void createAlert(Transaction txn, double score, RiskLevel level,
                              RuleBasedFraudDetector.FraudRuleResult ruleResult,
                              AiFraudAnalyzer.AiAnalysisResult aiResult) {
        FraudAlert alert = FraudAlert.builder()
                .transactionId(txn.getId())
                .userId(txn.getUserId())
                .riskScore(score)
                .riskLevel(level)
                .ruleViolations(String.join("\n", ruleResult.violations()))
                .aiAnalysis(aiResult.analysis())
                .alertStatus(AlertStatus.OPEN)
                .build();
        alertRepository.save(alert);
    }
}
