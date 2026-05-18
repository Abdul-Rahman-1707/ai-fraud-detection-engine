package com.portfolio.frauddetection.service;

import com.portfolio.frauddetection.dto.FraudAnalysisResult;
import com.portfolio.frauddetection.model.RiskLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudAlertNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastAlert(FraudAnalysisResult result) {
        if (result.getRiskLevel() == RiskLevel.HIGH || result.getRiskLevel() == RiskLevel.CRITICAL) {
            log.info("Broadcasting real-time fraud alert for txn {}: risk={}",
                    result.getTransactionId(), result.getRiskLevel());
            messagingTemplate.convertAndSend("/topic/fraud-alerts", result);
        }
    }

    public void broadcastDashboardUpdate(String eventType, Object payload) {
        log.debug("Broadcasting dashboard update: {}", eventType);
        messagingTemplate.convertAndSend("/topic/dashboard/" + eventType, payload);
    }
}
