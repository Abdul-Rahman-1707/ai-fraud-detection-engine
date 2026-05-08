package com.portfolio.frauddetection.service;

import com.portfolio.frauddetection.dto.*;
import com.portfolio.frauddetection.model.*;
import com.portfolio.frauddetection.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudAlertRepository alertRepository;
    private final FraudDetectionService fraudDetectionService;

    @Transactional
    public TransactionResponse processTransaction(TransactionRequest request) {
        Transaction transaction = Transaction.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .merchantName(request.getMerchantName())
                .merchantCategory(request.getMerchantCategory())
                .cardLast4(request.getCardLast4())
                .country(request.getCountry())
                .city(request.getCity())
                .ipAddress(request.getIpAddress())
                .deviceId(request.getDeviceId())
                .type(request.getType() != null ? request.getType() : TransactionType.PURCHASE)
                .status(TransactionStatus.PENDING)
                .build();

        transaction = transactionRepository.save(transaction);

        FraudAnalysisResult result = fraudDetectionService.analyzeTransaction(transaction);

        Transaction updated = transactionRepository.findById(transaction.getId()).orElse(transaction);

        return toResponse(updated);
    }

    public Page<TransactionResponse> getTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<TransactionResponse> getTransactionsByStatus(TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    public List<TransactionResponse> getUserTransactions(String userId) {
        return transactionRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    public TransactionResponse getTransaction(String id) {
        return transactionRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }

    public DashboardStats getDashboardStats() {
        return DashboardStats.builder()
                .totalTransactions(transactionRepository.count())
                .flaggedTransactions(transactionRepository.countByStatus(TransactionStatus.FLAGGED))
                .blockedTransactions(transactionRepository.countByStatus(TransactionStatus.BLOCKED))
                .approvedTransactions(transactionRepository.countByStatus(TransactionStatus.APPROVED))
                .totalAmountProcessed(transactionRepository.sumTotalAmount())
                .totalAmountBlocked(transactionRepository.sumAmountByStatus(TransactionStatus.BLOCKED))
                .avgFraudScore(transactionRepository.avgFraudScore() != null ? transactionRepository.avgFraudScore() : 0.0)
                .openAlerts(alertRepository.countByAlertStatus(AlertStatus.OPEN))
                .build();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .merchantName(t.getMerchantName())
                .merchantCategory(t.getMerchantCategory())
                .country(t.getCountry())
                .timestamp(t.getTimestamp())
                .status(t.getStatus())
                .fraudScore(t.getFraudScore())
                .fraudReason(t.getFraudReason())
                .build();
    }
}
