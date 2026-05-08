package com.portfolio.frauddetection.repository;

import com.portfolio.frauddetection.model.Transaction;
import com.portfolio.frauddetection.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByUserIdOrderByTimestampDesc(String userId);

    List<Transaction> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.timestamp > :since")
    long countRecentTransactions(@Param("userId") String userId, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.userId = :userId")
    BigDecimal findAverageAmountByUserId(@Param("userId") String userId);

    @Query("SELECT MAX(t.amount) FROM Transaction t WHERE t.userId = :userId")
    BigDecimal findMaxAmountByUserId(@Param("userId") String userId);

    long countByStatus(TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") TransactionStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t")
    BigDecimal sumTotalAmount();

    @Query("SELECT AVG(t.fraudScore) FROM Transaction t WHERE t.fraudScore IS NOT NULL")
    Double avgFraudScore();
}
