package com.portfolio.frauddetection.controller;

import com.portfolio.frauddetection.dto.TransactionSummary;
import com.portfolio.frauddetection.model.Transaction;
import com.portfolio.frauddetection.model.TransactionStatus;
import com.portfolio.frauddetection.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/daily-summary")
    public List<TransactionSummary> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String userId) {

        List<Transaction> transactions;
        if (userId != null) {
            transactions = transactionRepository.findByUserIdAndTimestampBetween(
                    userId, from.atStartOfDay(), to.atTime(LocalTime.MAX));
        } else {
            transactions = transactionRepository.findAll().stream()
                    .filter(t -> !t.getTimestamp().toLocalDate().isBefore(from)
                            && !t.getTimestamp().toLocalDate().isAfter(to))
                    .toList();
        }

        Map<LocalDate, List<Transaction>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTimestamp().toLocalDate()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> buildSummary(entry.getKey(), entry.getValue()))
                .toList();
    }

    private TransactionSummary buildSummary(LocalDate date, List<Transaction> txns) {
        return TransactionSummary.builder()
                .date(date)
                .totalCount(txns.size())
                .approvedCount(txns.stream().filter(t -> t.getStatus() == TransactionStatus.APPROVED).count())
                .blockedCount(txns.stream().filter(t -> t.getStatus() == TransactionStatus.BLOCKED).count())
                .flaggedCount(txns.stream().filter(t -> t.getStatus() == TransactionStatus.FLAGGED).count())
                .totalAmount(txns.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .blockedAmount(txns.stream()
                        .filter(t -> t.getStatus() == TransactionStatus.BLOCKED)
                        .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .avgFraudScore(txns.stream()
                        .filter(t -> t.getFraudScore() != null)
                        .mapToDouble(Transaction::getFraudScore).average().orElse(0.0))
                .maxFraudScore(txns.stream()
                        .filter(t -> t.getFraudScore() != null)
                        .mapToDouble(Transaction::getFraudScore).max().orElse(0.0))
                .build();
    }
}
