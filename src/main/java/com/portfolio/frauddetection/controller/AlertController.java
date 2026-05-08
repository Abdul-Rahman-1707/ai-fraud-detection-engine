package com.portfolio.frauddetection.controller;

import com.portfolio.frauddetection.dto.AlertResponse;
import com.portfolio.frauddetection.model.AlertStatus;
import com.portfolio.frauddetection.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping
    public Page<AlertResponse> getAlerts(
            @RequestParam(defaultValue = "OPEN") AlertStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return alertService.getAlerts(status, pageable);
    }

    @GetMapping("/user/{userId}")
    public List<AlertResponse> getUserAlerts(@PathVariable String userId) {
        return alertService.getUserAlerts(userId);
    }

    @PatchMapping("/{id}/status")
    public AlertResponse updateAlertStatus(
            @PathVariable String id,
            @RequestParam AlertStatus status,
            @RequestParam(defaultValue = "system") String reviewer) {
        return alertService.updateAlertStatus(id, status, reviewer);
    }
}
