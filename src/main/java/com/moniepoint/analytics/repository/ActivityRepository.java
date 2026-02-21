package com.moniepoint.analytics.repository;

import com.moniepoint.analytics.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    
    @Query("SELECT a.merchantId, SUM(a.amount) as totalVolume FROM Activity a WHERE a.status = 'SUCCESS' GROUP BY a.merchantId ORDER BY totalVolume DESC")
    List<Object[]> findTopMerchant();

    @Query("SELECT FORMATDATETIME(a.eventTimestamp, 'yyyy-MM') as month, COUNT(DISTINCT a.merchantId) FROM Activity a WHERE a.status = 'SUCCESS' GROUP BY month ORDER BY month")
    List<Object[]> countMonthlyActiveMerchants();

    @Query("SELECT a.product, COUNT(DISTINCT a.merchantId) as uniqueCount FROM Activity a GROUP BY a.product ORDER BY uniqueCount DESC")
    List<Object[]> countProductAdoption();

    @Query("SELECT a.eventType, COUNT(DISTINCT a.merchantId) FROM Activity a WHERE a.product = 'KYC' AND a.status = 'SUCCESS' GROUP BY a.eventType")
    List<Object[]> kycFunnel();

    @Query("SELECT a.product, " +
           "SUM(CASE WHEN a.status = 'FAILED' THEN 1 ELSE 0 END) * 100.0 / " +
           "NULLIF(SUM(CASE WHEN a.status IN ('SUCCESS', 'FAILED') THEN 1 ELSE 0 END), 0) as failureRate " +
           "FROM Activity a " +
           "WHERE a.status IN ('SUCCESS', 'FAILED') " +
           "GROUP BY a.product " +
           "ORDER BY failureRate DESC")
    List<Object[]> calculateFailureRates();
}
