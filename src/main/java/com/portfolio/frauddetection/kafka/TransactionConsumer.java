package com.portfolio.frauddetection.kafka;

import com.portfolio.frauddetection.dto.TransactionRequest;
import com.portfolio.frauddetection.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionConsumer {

    private final TransactionService transactionService;

    @KafkaListener(topics = "${app.kafka.topics.transactions}", groupId = "fraud-detection-group")
    public void consumeTransaction(TransactionRequest request) {
        log.info("Received transaction from Kafka: userId={}, amount={}, merchant={}",
                request.getUserId(), request.getAmount(), request.getMerchantName());

        try {
            var response = transactionService.processTransaction(request);
            log.info("Processed transaction {}: status={}, fraudScore={}",
                    response.getId(), response.getStatus(), response.getFraudScore());
        } catch (Exception e) {
            log.error("Failed to process transaction for user {}: {}",
                    request.getUserId(), e.getMessage(), e);
        }
    }
}
