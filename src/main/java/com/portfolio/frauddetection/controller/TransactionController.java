package com.portfolio.frauddetection.controller;

import com.portfolio.frauddetection.dto.DashboardStats;
import com.portfolio.frauddetection.dto.TransactionRequest;
import com.portfolio.frauddetection.dto.TransactionResponse;
import com.portfolio.frauddetection.model.TransactionStatus;
import com.portfolio.frauddetection.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse processTransaction(@Valid @RequestBody TransactionRequest request) {
        return transactionService.processTransaction(request);
    }

    @GetMapping
    public Page<TransactionResponse> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        if (status != null) {
            return transactionService.getTransactionsByStatus(status, pageable);
        }
        return transactionService.getTransactions(pageable);
    }

    @GetMapping("/{id}")
    public TransactionResponse getTransaction(@PathVariable String id) {
        return transactionService.getTransaction(id);
    }

    @GetMapping("/user/{userId}")
    public List<TransactionResponse> getUserTransactions(@PathVariable String userId) {
        return transactionService.getUserTransactions(userId);
    }

    @GetMapping("/dashboard/stats")
    public DashboardStats getDashboardStats() {
        return transactionService.getDashboardStats();
    }
}
