package com.moniepoint.analytics.controller;

import com.moniepoint.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/top-merchant")
    public ResponseEntity<Map<String, Object>> getTopMerchant() {
        return ResponseEntity.ok(analyticsService.getTopMerchant());
    }

    @GetMapping("/monthly-active-merchants")
    public ResponseEntity<Map<String, Long>> getMonthlyActiveMerchants() {
        return ResponseEntity.ok(analyticsService.getMonthlyActiveMerchants());
    }

    @GetMapping("/product-adoption")
    public ResponseEntity<Map<String, Long>> getProductAdoption() {
        return ResponseEntity.ok(analyticsService.getProductAdoption());
    }

    @GetMapping("/kyc-funnel")
    public ResponseEntity<Map<String, Long>> getKycFunnel() {
        return ResponseEntity.ok(analyticsService.getKycFunnel());
    }

    @GetMapping("/failure-rates")
    public ResponseEntity<List<Map<String, Object>>> getFailureRates() {
        return ResponseEntity.ok(analyticsService.getFailureRates());
    }
}
