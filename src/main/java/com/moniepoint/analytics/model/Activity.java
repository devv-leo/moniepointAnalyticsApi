package com.moniepoint.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(name = "event_timestamp")
    private LocalDateTime eventTimestamp;

    private String product;

    @Column(name = "event_type")
    private String eventType;

    private BigDecimal amount;
    private String status;
    private String channel;
    private String region;

    @Column(name = "merchant_tier")
    private String merchantTier;
}
