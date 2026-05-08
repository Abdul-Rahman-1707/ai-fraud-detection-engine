package com.portfolio.frauddetection.engine;

import com.portfolio.frauddetection.model.Transaction;
import com.portfolio.frauddetection.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiFraudAnalyzer {

    private final ChatClient.Builder chatClientBuilder;

    public AiAnalysisResult analyze(Transaction transaction, UserProfile profile,
                                     RuleBasedFraudDetector.FraudRuleResult ruleResult) {
        String prompt = buildPrompt(transaction, profile, ruleResult);

        try {
            String response = chatClientBuilder.build()
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            double aiScore = extractScore(response);
            log.info("AI analysis for txn {}: score={}", transaction.getId(), aiScore);

            return new AiAnalysisResult(aiScore, response);
        } catch (Exception e) {
            log.error("AI analysis failed for txn {}: {}", transaction.getId(), e.getMessage());
            return new AiAnalysisResult(ruleResult.score(), "AI analysis unavailable: " + e.getMessage());
        }
    }

    private String buildPrompt(Transaction txn, UserProfile profile,
                                RuleBasedFraudDetector.FraudRuleResult ruleResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a payment fraud detection AI. Analyze this transaction and respond with:\n");
        sb.append("1. A fraud risk score between 0.0 (safe) and 1.0 (definitely fraud)\n");
        sb.append("2. A brief explanation of your reasoning\n\n");
        sb.append("Format: Start your response with SCORE: followed by the number, then ANALYSIS: followed by explanation.\n\n");
        sb.append("Transaction Details:\n");
        sb.append("- Amount: $").append(txn.getAmount()).append(" ").append(txn.getCurrency()).append("\n");
        sb.append("- Merchant: ").append(txn.getMerchantName()).append(" (").append(txn.getMerchantCategory()).append(")\n");
        sb.append("- Location: ").append(txn.getCity()).append(", ").append(txn.getCountry()).append("\n");
        sb.append("- Time: ").append(txn.getTimestamp()).append("\n");
        sb.append("- Card: ****").append(txn.getCardLast4()).append("\n");

        if (profile != null) {
            sb.append("\nUser Profile:\n");
            sb.append("- Avg transaction: $").append(profile.getAvgTransactionAmount()).append("\n");
            sb.append("- Max transaction: $").append(profile.getMaxTransactionAmount()).append("\n");
            sb.append("- Primary country: ").append(profile.getPrimaryCountry()).append("\n");
            sb.append("- Total transactions: ").append(profile.getTotalTransactions()).append("\n");
            sb.append("- Previously flagged: ").append(profile.getFlaggedTransactions()).append("\n");
        }

        if (!ruleResult.violations().isEmpty()) {
            sb.append("\nRule Violations Detected:\n");
            ruleResult.violations().forEach(v -> sb.append("- ").append(v).append("\n"));
        }

        return sb.toString();
    }

    private double extractScore(String response) {
        try {
            int scoreIdx = response.indexOf("SCORE:");
            if (scoreIdx >= 0) {
                String afterScore = response.substring(scoreIdx + 6).trim();
                String scoreStr = afterScore.split("[\\s\\n]")[0];
                return Double.parseDouble(scoreStr);
            }
        } catch (NumberFormatException e) {
            log.warn("Could not parse AI score from response");
        }
        return 0.5;
    }

    public record AiAnalysisResult(double score, String analysis) {}
}
