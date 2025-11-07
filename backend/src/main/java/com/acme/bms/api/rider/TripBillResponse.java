package com.acme.bms.api.rider;

import com.acme.bms.domain.entity.Bill;
import com.acme.bms.domain.entity.BikeType;
import java.time.Instant;
import java.time.ZoneId;
import java.time.Duration;

public record TripBillResponse(
        Long billId,
        Long tripId,
        double totalAmount,
        Instant createdAt,
        Instant startTime,
        Instant endTime,
        long durationMinutes,
        double baseFee,
        double usageCost,
        double electricCharge,
        double discountAmount,
        Long endStationId,
        String endStationName,
        TripInfoResponse trip
) {
    public static TripBillResponse from(Bill bill, TripInfoResponse trip) {
        var tripEntity = bill.getTrip();
        Instant start = tripEntity.getStartTime() == null ? null : tripEntity.getStartTime().atZone(ZoneId.systemDefault()).toInstant();
        Instant end = tripEntity.getEndTime() == null ? null : tripEntity.getEndTime().atZone(ZoneId.systemDefault()).toInstant();
        long minutes = 0;
        if (start != null && end != null) {
            minutes = Duration.between(start, end).toMinutes();
        }

        // reproduce builder pricing logic (kept in sync with builders)
        boolean isStudent = false;
        var rider = tripEntity.getRider();
        if (rider != null && rider.getEmail() != null && rider.getEmail().toLowerCase().endsWith(".edu")) {
            isStudent = true;
        }

        double base = isStudent ? 2.0 : 2.5;
        double usageRate = isStudent ? 0.10 : 0.15;
        double eBikeMultiplier = 1.5;
        double discountRate = isStudent ? 0.10 : 0.0;

        double usage = minutes * usageRate;
        double subtotal = base + usage;
        double electricCharge = 0.0;
        if (tripEntity.getBike() != null && tripEntity.getBike().getType() == BikeType.ELECTRIC) {
            double withElectric = subtotal * eBikeMultiplier;
            electricCharge = withElectric - subtotal;
            subtotal = withElectric;
        }

        double discount = subtotal * discountRate;
        double total = subtotal - discount;

        Long endStationId = null;
        String endStationName = null;
        if (tripEntity.getEndStation() != null) {
            endStationId = tripEntity.getEndStation().getId();
            endStationName = tripEntity.getEndStation().getName();
        }

        return new TripBillResponse(
                bill.getId(),
                tripEntity.getId(),
                total,
                bill.getCreatedAt() == null ? null : bill.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant(),
                start,
                end,
                minutes,
                base,
                usage,
                electricCharge,
                discount,
                endStationId,
                endStationName,
                trip
        );
    }
}
