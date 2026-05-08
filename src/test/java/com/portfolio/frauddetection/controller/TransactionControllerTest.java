package com.portfolio.frauddetection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.frauddetection.dto.DashboardStats;
import com.portfolio.frauddetection.dto.TransactionRequest;
import com.portfolio.frauddetection.dto.TransactionResponse;
import com.portfolio.frauddetection.model.TransactionStatus;
import com.portfolio.frauddetection.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private TransactionService transactionService;

    @Test
    void processTransaction_validRequest_shouldReturn201() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .userId("user-123")
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .merchantName("Amazon")
                .merchantCategory("RETAIL")
                .country("US")
                .build();

        TransactionResponse response = TransactionResponse.builder()
                .id("txn-001")
                .userId("user-123")
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .merchantName("Amazon")
                .status(TransactionStatus.APPROVED)
                .fraudScore(0.1)
                .timestamp(LocalDateTime.now())
                .build();

        when(transactionService.processTransaction(any())).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("txn-001"))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void processTransaction_missingUserId_shouldReturn400() throws Exception {
        TransactionRequest request = TransactionRequest.builder()
                .amount(new BigDecimal("250.00"))
                .currency("USD")
                .merchantName("Amazon")
                .merchantCategory("RETAIL")
                .country("US")
                .build();

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getDashboardStats_shouldReturnStats() throws Exception {
        DashboardStats stats = DashboardStats.builder()
                .totalTransactions(1000)
                .flaggedTransactions(50)
                .blockedTransactions(20)
                .approvedTransactions(930)
                .totalAmountProcessed(new BigDecimal("500000.00"))
                .totalAmountBlocked(new BigDecimal("25000.00"))
                .avgFraudScore(0.12)
                .openAlerts(15)
                .build();

        when(transactionService.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/transactions/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTransactions").value(1000))
                .andExpect(jsonPath("$.openAlerts").value(15));
    }
}
