package com.moniepoint.analytics.service;

import com.moniepoint.analytics.model.Activity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class ActivityImporter {
    
    // Some timestamps are empty in the sample, so we need some flexibility here.
    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    public static Activity parseRow(String[] row) {
        // Checking for length first because some rows might just be weirdly empty.
        if (row.length < 10) return null;

        // I'm being super defensive here. Real-world data is usually a mess.
        // If we don't have an event ID, the record is pretty much trash for us.
        String eventIdStr = row[0].trim();
        if (eventIdStr.isEmpty()) return null;

        BigDecimal amount = BigDecimal.ZERO;
        try {
            if (!row[5].trim().isEmpty()) {
                amount = new BigDecimal(row[5].trim());
            }
        } catch (NumberFormatException e) {
            // If the amount is junk, I'll just set it to 0. 
            // Better than crashing, and usually non-monetary events have 0 anyway.
        }

        LocalDateTime timestamp = null;
        try {
            if (!row[2].trim().isEmpty()) {
                timestamp = LocalDateTime.parse(row[2].trim(), ISO_FORMAT);
            } else {
                // I noticed some sample data has empty timestamps for POS transactions.
                // I'm going to let them in as null and handle it in the analytics logic.
                // It's a bummer, but reality is messy!
            }
        } catch (Exception e) {
            // If the date is unparseable, I'm just leaving it null.
        }

        return Activity.builder()
                .eventId(UUID.fromString(eventIdStr))
                .merchantId(row[1].trim())
                .eventTimestamp(timestamp)
                .product(row[3].trim())
                .eventType(row[4].trim())
                .amount(amount)
                .status(row[6].trim())
                .channel(row[7].trim())
                .region(row[8].trim())
                .merchantTier(row[9].trim())
                .build();
    }
}
