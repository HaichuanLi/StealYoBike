package com.acme.bms.application.usecase;

import java.time.Duration;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.acme.bms.api.trip.TripResponse;
import com.acme.bms.domain.entity.*;
import com.acme.bms.domain.repo.*;

@Service
@RequiredArgsConstructor
public class UC16_SearchRidebyTripID {

    private final TripRepository tripRepository;
    private final PricingPlanRepository pricingPlanRepository;

    public TripResponse execute(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));

        // Compute duration
        long durationMinutes = 0;
        if (trip.getStartTime() != null && trip.getEndTime() != null) {
            durationMinutes = Duration.between(trip.getStartTime(), trip.getEndTime()).toMinutes();
        }

        // Fetch pricing plan
        PricingPlan plan = pricingPlanRepository.findByType(trip.getBike().getType() == BikeType.ELECTRIC
                        ? PricingPlanType.EBIKE
                        : PricingPlanType.STANDARD)
                .orElseThrow(() -> new IllegalArgumentException("Pricing plan not found"));

        double baseFee = plan.getBaseFee();
        double perMinuteFee = plan.getPerMinuteRate();
        double eBikeSurcharge = trip.getBike().getType() == BikeType.ELECTRIC
                ? (plan.getSurcharge() != null ? plan.getSurcharge() : 0.0)
                : 0.0;

        double totalCost = baseFee + (perMinuteFee * durationMinutes) + eBikeSurcharge;
        double discountAmount = 0.0;

        // Timeline
        String timeline = String.format("%s: checkout → %s: return",
                trip.getStartTime() != null ? trip.getStartTime() : "N/A",
                trip.getEndTime() != null ? trip.getEndTime() : "N/A"
        );

        String planStr = trip.getRider() != null && trip.getRider().getPlan() != null ? trip.getRider().getPlan().name() : "N/A";

        return new TripResponse(
                trip.getId(),
                trip.getRider() != null ? trip.getRider().getUsername() : "N/A",
                trip.getStartStation() != null ? trip.getStartStation().getName() : "N/A",
                trip.getEndStation() != null ? trip.getEndStation().getName() : "N/A",
                trip.getStartTime(),
                trip.getEndTime(),
                durationMinutes,
                trip.getBike() != null ? trip.getBike().getType().name() : "N/A",
                planStr,
                baseFee,
                perMinuteFee,
                eBikeSurcharge,
                discountAmount,
                totalCost,
                timeline
        );
    }
}