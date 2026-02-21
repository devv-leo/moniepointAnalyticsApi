package com.moniepoint.analytics.service;

import com.moniepoint.analytics.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ActivityRepository repository;

    public Map<String, Object> getTopMerchant() {
        // I'm hitting the DB to get all successful volumes grouped by merchant.
        // It's more efficient than pulling everything and doing it in Java.
        List<Object[]> results = repository.findTopMerchant();
        if (results.isEmpty())
            return Collections.emptyMap();

        // Grab the first one since I ordered it DESC in the query.
        Object[] top = results.get(0);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("merchant_id", top[0]);
        // Spec specifically asks for 2 decimal places. Gotta be precise!
        response.put("total_volume", ((BigDecimal) top[1]).setScale(2, RoundingMode.HALF_UP));
        return response;
    }

    public Map<String, Long> getMonthlyActiveMerchants() {
        // I'm using a TreeMap here because I want those months sorted properly in the
        // JSON.
        // Nothing worse than a jumbled timeline in a dashboard!
        List<Object[]> results = repository.countMonthlyActiveMerchants();
        return results.stream()
                .filter(r -> r[0] != null)  // Filter out null month values
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> ((Number) r[1]).longValue(),
                        (v1, v2) -> v1,
                        TreeMap::new));
    }

    public Map<String, Long> getProductAdoption() {
        // Here I'm just counting unique merchants per product.
        // It helps the Growth team see which features are stickiest.
        List<Object[]> results = repository.countProductAdoption();
        return results.stream()
                .collect(Collectors.toMap(
                        r -> (String) r[0],
                        r -> (Long) r[1],
                        (v1, v2) -> v1,
                        LinkedHashMap::new));
    }

    public Map<String, Long> getKycFunnel() {
        // Funnels are cool. I'm taking the raw event counts and mapping them
        // to a more descriptive output that the intelligence team can actually use.
        List<Object[]> results = repository.kycFunnel();
        Map<String, Long> rawCounts = results.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> (Long) r[1]));

        Map<String, Long> funnel = new LinkedHashMap<>();
        funnel.put("documents_submitted", rawCounts.getOrDefault("DOCUMENT_SUBMITTED", 0L));
        funnel.put("verifications_completed", rawCounts.getOrDefault("VERIFICATION_COMPLETED", 0L));
        funnel.put("tier_upgrades", rawCounts.getOrDefault("TIER_UPGRADE", 0L));
        return funnel;
    }

    public List<Map<String, Object>> getFailureRates() {
        // Failure rates are a great health check.
        // I'm doing the percentage math in the SQL query to keep things snappy.
        List<Object[]> results = repository.calculateFailureRates();
        return results.stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("product", r[0]);
                    // Spec asked for 1 decimal place here.
                    Number failureRateNumber = (Number) r[1];
                    BigDecimal failureRate = failureRateNumber instanceof BigDecimal 
                        ? (BigDecimal) failureRateNumber 
                        : new BigDecimal(failureRateNumber.doubleValue());
                    map.put("failure_rate", failureRate.setScale(1, RoundingMode.HALF_UP));
                    return map;
                })
                .collect(Collectors.toList());
    }
}
