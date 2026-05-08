package com.portfolio.frauddetection.kafka;

import com.portfolio.frauddetection.dto.FraudAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAlertProducer {

    private final KafkaTemplate<String, FraudAnalysisResult> kafkaTemplate;

    @Value("${app.kafka.topics.fraud-alerts}")
    private String fraudAlertsTopic;

    public void publishFraudAlert(FraudAnalysisResult result) {
        log.info("Publishing fraud alert for txn {}: risk={}", result.getTransactionId(), result.getRiskLevel());
        kafkaTemplate.send(fraudAlertsTopic, result.getTransactionId(), result);
    }
}
