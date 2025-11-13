package com.acme.bms.api.rider;

import com.acme.bms.domain.entity.Bill;
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
    boolean paid,
    String paymentTokenUsed,
    Instant paidAt,
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

        // Use persisted bill component fields when available so the API reflects the stored bill
        double base = bill.getBaseFee();
        double usage = bill.getUsageCost();
        double electricCharge = bill.getElectricCharge();
        double discount = bill.getDiscountAmount();
        double total = bill.getTotalAmount();

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
        bill.isPaid(),
        bill.getPaymentTokenUsed(),
        bill.getPaidAt() == null ? null : bill.getPaidAt().atZone(ZoneId.systemDefault()).toInstant(),
        trip
    );
    }
}
