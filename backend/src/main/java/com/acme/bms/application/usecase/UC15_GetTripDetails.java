package com.acme.bms.application.usecase;

import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.trip.TripResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.repo.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC15_GetTripDetails {

    private final TripRepository tripRepo;
    private final BillRepository billRepo;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional(readOnly = true)
    public TripResponse forOperator(Long tripId) {
        Trip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));
        return toResponse(t);
    }

    @Transactional(readOnly = true)
    public TripResponse forRider(Long riderId, Long tripId) {
        Trip t = tripRepo.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));
        if (t.getRider() == null || !t.getRider().getId().equals(riderId)) {
            throw new IllegalStateException("Trip does not belong to rider");
        }
        return toResponse(t);
    }

    private TripResponse toResponse(Trip t) {
        Long tripId = t.getId();

        // Rider & stations
        String riderName     = t.getRider() != null ? t.getRider().getUsername() : null;
        String startStation  = t.getStartStation() != null ? t.getStartStation().getName() : null;
        String endStation    = t.getEndStation() != null ? t.getEndStation().getName()   : null;

        // Times
        var startTime = t.getStartTime();   // LocalDateTime or null
        var endTime   = t.getEndTime();     // LocalDateTime or null

        // Duration (long primitive returned in TripResponse; compute 0 if unknown)
        long durationMinutes = 0L;
        if (startTime != null && endTime != null) {
            durationMinutes = Duration.between(startTime, endTime).toMinutes();
        }

        // Bike type
        Bike bike = t.getBike();
        String bikeType = (bike != null && bike.getType() != null) ? bike.getType().name() : null;
    String plan = t.getRider() != null && t.getRider().getPlan() != null ? t.getRider().getPlan().name() : null;

        // Cost breakdown: prefer persisted Bill component fields if present
        double baseFee = 0.0;
        double perMinuteFee = 0.0;
        double eBikeSurcharge = 0.0;
        double discountAmount = 0.0;
        double tierDiscountAmount = 0.0;
        double totalCost = 0.0;
        String tier = "REGULAR";

        var billOpt = billRepo.findByTripId(tripId);
        if (billOpt.isPresent()) {
            var b = billOpt.get();
            totalCost = b.getTotalAmount();
            baseFee = b.getBaseFee();
            perMinuteFee = b.getUsageCost();
            eBikeSurcharge = b.getElectricCharge();
            discountAmount = b.getDiscountAmount();
            tierDiscountAmount = b.getTierDiscountAmount();
            if (t.getRider() != null && t.getRider().getTier() != null) {
                tier = t.getRider().getTier().toString();
            }
        } else {
            Integer cents = t.getPriceCents();
            totalCost = (cents != null ? cents : 0) / 100.0;
        }

        // Simple timeline string: "Checkout: <ts> → Return: <ts>"
        String timeline = (startTime != null ? "Checkout: " + startTime.format(FMT) : "Checkout: —")
                + " \u2192 "
                + (endTime != null ? "Return: " + endTime.format(FMT) : "Return: —");

    return new TripResponse(
                tripId,
                riderName,
                startStation,
                endStation,
                startTime,
                endTime,
                durationMinutes,
                bikeType,
        plan,
                baseFee,
        perMinuteFee,
        eBikeSurcharge,
        discountAmount,
        tierDiscountAmount,
        tier,
        totalCost,
                timeline
        );
    }
}
