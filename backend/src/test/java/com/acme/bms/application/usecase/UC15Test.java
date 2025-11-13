package com.acme.bms.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import com.acme.bms.api.trip.TripResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.repo.BillRepository;
import com.acme.bms.domain.repo.TripRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class UC15Test {

    @Mock TripRepository tripRepo;
    @Mock BillRepository billRepo;

    UC15_GetTripDetails uc15;

    @BeforeEach
    void setup() {
        uc15 = new UC15_GetTripDetails(tripRepo, billRepo);
    }

    @Test
    void forOperator_withBill_present_returnsTotalAndTimeline() {
        // Arrange (build entities with Lombok @Builder)
        Long tripId = 1001L;
        User rider = User.builder()
                .id(10L)
                .username("alice")
                .build();

        Bike bike = Bike.builder()
                .id(7L)
                .type(BikeType.ELECTRIC)
                .build();

        DockingStation start = DockingStation.builder()
                .id(1L)
                .name("Downtown")
                .build();

        DockingStation end = DockingStation.builder()
                .id(2L)
                .name("Uptown")
                .build();

        LocalDateTime startAt = LocalDateTime.now().minusMinutes(17);
        LocalDateTime endAt = LocalDateTime.now();

        Trip trip = Trip.builder()
                .id(tripId)
                .rider(rider)
                .bike(bike)
                .startStation(start)
                .endStation(end)
                .startTime(startAt)
                .endTime(endAt)
                .priceCents(0)
                .build();

        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));

        // Mock a Bill only for totalAmount; UC15 will pick it up.
        var bill = mock(com.acme.bms.domain.entity.Bill.class);
        when(bill.getTotalAmount()).thenReturn(5.50);
        when(billRepo.findByTripId(tripId)).thenReturn(Optional.of(bill));

        // BEFORE
        System.out.println("=== BEFORE (Operator / Bill present) ===");
        System.out.printf("TripId=%d rider=%s bikeType=%s start=%s end=%s startAt=%s endAt=%s%n",
                tripId, rider.getUsername(), bike.getType().name(), start.getName(), end.getName(), startAt, endAt);
        System.out.println("Bill: total=5.50 (breakdown fields may be 0.0 if Bill lacks getters)");

        // Act
        TripResponse out = uc15.forOperator(tripId);

        // AFTER
        System.out.println("=== AFTER (Operator / Bill present) ===");
        System.out.println("TripResponse: "
                + "tripId=" + out.tripId()
                + ", rider=" + out.riderName()
                + ", bikeType=" + out.bikeType()
                + ", startStation=" + out.startStation()
                + ", endStation=" + out.endStation()
                + ", duration=" + out.durationMinutes()
                + ", baseFee=" + out.baseFee()
                + ", perMinuteFee=" + out.perMinuteFee()
                + ", eBikeSurcharge=" + out.eBikeSurcharge()
                + ", totalCost=" + out.totalCost()
                + ", timeline=" + out.timeline()
        );

        // Assert
        assertEquals(tripId, out.tripId());
        assertEquals("alice", out.riderName());
        assertEquals("ELECTRIC", out.bikeType());
        assertEquals("Downtown", out.startStation());
        assertEquals("Uptown", out.endStation());
        assertTrue(out.durationMinutes() >= 16 && out.durationMinutes() <= 18, "duration ~17m");

        assertEquals(5.50, out.totalCost(), 1e-9);
        assertNotNull(out.timeline());
        assertTrue(out.timeline().contains("Checkout"));
        assertTrue(out.timeline().contains("Return"));
    }

    @Test
    void forRider_withoutBill_fallsBackToPriceCents_andEnforcesOwnership() {
        // Arrange
        Long tripId = 2002L;
        User rider = User.builder()
                .id(20L)
                .username("bob")
                .build();

        Bike bike = Bike.builder()
                .id(9L)
                .type(BikeType.REGULAR)
                .build();

        DockingStation start = DockingStation.builder()
                .id(3L)
                .name("University")
                .build();

        DockingStation end = DockingStation.builder()
                .id(4L)
                .name("Harbor")
                .build();

        LocalDateTime startAt = LocalDateTime.now().minusMinutes(10);
        LocalDateTime endAt = LocalDateTime.now();

        Trip trip = Trip.builder()
                .id(tripId)
                .rider(rider)
                .bike(bike)
                .startStation(start)
                .endStation(end)
                .startTime(startAt)
                .endTime(endAt)
                .priceCents(375)
                .build();

        when(tripRepo.findById(tripId)).thenReturn(Optional.of(trip));
        when(billRepo.findByTripId(tripId)).thenReturn(Optional.empty());

        // BEFORE
        System.out.println("=== BEFORE (Rider / No bill) ===");
        System.out.printf("TripId=%d rider=%s bikeType=%s priceCents=%d%n",
                tripId, rider.getUsername(), bike.getType().name(), 375);

        // Act
        TripResponse out = uc15.forRider(20L, tripId);

        // AFTER
        System.out.println("=== AFTER (Rider / No bill) ===");
        System.out.println("TripResponse: "
                + "tripId=" + out.tripId()
                + ", rider=" + out.riderName()
                + ", bikeType=" + out.bikeType()
                + ", totalCost=" + out.totalCost()
                + ", baseFee=" + out.baseFee()
                + ", perMinuteFee=" + out.perMinuteFee()
                + ", eBikeSurcharge=" + out.eBikeSurcharge()
                + ", timeline=" + out.timeline()
        );

        // Assert (fallback used)
        assertEquals(tripId, out.tripId());
        assertEquals("bob", out.riderName());
        assertEquals("REGULAR", out.bikeType());
        assertEquals(3.75, out.totalCost(), 1e-9);
        // No bill -> breakdown 0.0
        assertEquals(0.0, out.baseFee(), 1e-9);
        assertEquals(0.0, out.perMinuteFee(), 1e-9);
        assertEquals(0.0, out.eBikeSurcharge(), 1e-9);

        // Ownership enforced
        assertThrows(IllegalStateException.class, () -> uc15.forRider(999L, tripId));
    }

    @Test
    void forOperator_tripNotFound_throws() {
        when(tripRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> uc15.forOperator(42L));
    }
}
