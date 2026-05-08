package com.portfolio.frauddetection.repository;

import com.portfolio.frauddetection.model.AlertStatus;
import com.portfolio.frauddetection.model.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {

    Page<FraudAlert> findByAlertStatusOrderByCreatedAtDesc(AlertStatus status, Pageable pageable);

    List<FraudAlert> findByUserIdOrderByCreatedAtDesc(String userId);

    long countByAlertStatus(AlertStatus status);

    List<FraudAlert> findByTransactionId(String transactionId);
}
