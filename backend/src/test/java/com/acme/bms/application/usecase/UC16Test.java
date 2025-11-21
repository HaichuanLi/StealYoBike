package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.acme.bms.api.trip.TripResponse;
import com.acme.bms.domain.entity.*;
import com.acme.bms.domain.repo.PricingPlanRepository;
import com.acme.bms.domain.repo.TripRepository;

class UC16Test {

    @Test
    void execute_shouldReturnDetailedTripResponse_whenTripExists() {
        System.out.println("\n===== TEST CASE: Trip Found with Full Details =====");

        TripRepository mockTripRepo = mock(TripRepository.class);
        PricingPlanRepository mockPricingRepo = mock(PricingPlanRepository.class);
        UC16_SearchRidebyTripID useCase = new UC16_SearchRidebyTripID(mockTripRepo, mockPricingRepo, null);

        // Rider & Bike
        User rider = User.builder().id(10L).username("Alice").build();
        Bike bike = Bike.builder().id(20L).type(BikeType.ELECTRIC).build();
        DockingStation startStation = DockingStation.builder().id(1L).name("Station A").build();
        DockingStation endStation = DockingStation.builder().id(2L).name("Station B").build();

        LocalDateTime start = LocalDateTime.of(2025, 11, 6, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 6, 10, 30);

        Trip trip = Trip.builder()
                .id(99L)
                .rider(rider)
                .bike(bike)
                .startStation(startStation)
                .endStation(endStation)
                .startTime(start)
                .endTime(end)
                .priceCents(0) // ignored; cost computed from pricing plan
                .build();

        when(mockTripRepo.findById(99L)).thenReturn(Optional.of(trip));

        // Pricing plan
        PricingPlan eBikePlan = PricingPlan.builder()
                .type(PricingPlanType.EBIKE)
                .baseFee(2.50)
                .perMinuteRate(0.25)
                .surcharge(1.00)
                .build();
        when(mockPricingRepo.findByType(PricingPlanType.EBIKE)).thenReturn(Optional.of(eBikePlan));

        // ===== BEFORE EXECUTION (Trip entity) =====
        System.out.println("===== BEFORE execution (Trip entity from DB) =====");
        System.out.println("Trip ID: " + trip.getId());
        System.out.println("Rider: " + trip.getRider().getUsername());
        System.out.println("Bike Type: " + trip.getBike().getType().name());
        System.out.println("Start Station: " + trip.getStartStation().getName());
        System.out.println("End Station: " + trip.getEndStation().getName());
        System.out.println("Start Time: " + trip.getStartTime());
        System.out.println("End Time: " + trip.getEndTime());
        System.out.println("Price Cents: " + trip.getPriceCents());

        // ===== EXECUTION =====
        TripResponse response = useCase.execute(99L);

        // ===== AFTER EXECUTION (TripResponse) =====
        System.out.println("\n===== AFTER execution (TripResponse) =====");
        System.out.println("Trip ID: " + response.tripId());
        System.out.println("Rider: " + response.riderName());
        System.out.println("Bike Type: " + response.bikeType());
        System.out.println("Start Station: " + response.startStation());
        System.out.println("End Station: " + response.endStation());
        System.out.println("Start Time: " + response.startTime());
        System.out.println("End Time: " + response.endTime());
        System.out.println("Duration (min): " + response.durationMinutes());
        System.out.println("Base Fee: " + response.baseFee());
        System.out.println("Per-Minute Fee: " + response.perMinuteFee());
        System.out.println("E-Bike Surcharge: " + response.eBikeSurcharge());
        System.out.println("Total Cost: " + response.totalCost());
        System.out.println("Timeline: " + response.timeline());

        // Assertions
        assertThat(response.tripId()).isEqualTo(99L);
        assertThat(response.riderName()).isEqualTo("Alice");
        assertThat(response.startStation()).isEqualTo("Station A");
        assertThat(response.endStation()).isEqualTo("Station B");
        assertThat(response.durationMinutes()).isEqualTo(30);
        assertThat(response.bikeType()).isEqualTo("ELECTRIC");
        assertThat(response.baseFee()).isEqualTo(2.50);
        assertThat(response.perMinuteFee()).isEqualTo(0.25);
        assertThat(response.eBikeSurcharge()).isEqualTo(1.00);
        assertThat(response.totalCost()).isEqualTo(2.50 + 0.25 * 30 + 1.00);
        assertThat(response.timeline()).isEqualTo("2025-11-06T10:00: checkout → 2025-11-06T10:30: return");
    }

    @Test
    void execute_shouldThrowException_whenTripNotFound() {
        System.out.println("\n===== TEST CASE: Trip NOT Found =====");

        TripRepository mockTripRepo = mock(TripRepository.class);
        PricingPlanRepository mockPricingRepo = mock(PricingPlanRepository.class);
        UC16_SearchRidebyTripID useCase = new UC16_SearchRidebyTripID(mockTripRepo, mockPricingRepo, null);

        when(mockTripRepo.findById(123L)).thenReturn(Optional.empty());

        System.out.println("===== BEFORE execution =====");
        System.out.println("Attempting to execute use case for non-existing Trip ID 123");

        try {
            useCase.execute(123L);
            System.out.println("ERROR: Exception not thrown!");
        } catch (IllegalArgumentException ex) {
            System.out.println("===== AFTER execution =====");
            System.out.println("Exception caught as expected → " + ex.getMessage());
            assertThat(ex.getMessage()).isEqualTo("Trip not found");
        }
    }
}
