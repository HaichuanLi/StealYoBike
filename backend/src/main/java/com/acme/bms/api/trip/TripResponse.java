package com.acme.bms.api.trip;

import java.time.LocalDateTime;

public record TripResponse(
        Long tripId,
        String riderName,
        String startStation,
        String endStation,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long durationMinutes,
        String bikeType,
        String plan,
        double baseFee,
        double perMinuteFee,
        double eBikeSurcharge,
        double discountAmount,
        double tierDiscountAmount,
        String tier,
        double totalCost,
        String timeline
) {}