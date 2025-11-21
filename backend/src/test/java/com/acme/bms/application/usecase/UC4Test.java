package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.acme.bms.api.rider.ReturnBikeRequest;
import com.acme.bms.api.rider.ReturnBikeResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.repo.DockRepository;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.TripRepository;

class UC4Test {

    @Test
    void execute_returnsBike_andCompletesTrip() {
        System.out.println("\n=== UC4: Return Bike Use Case ===");

        // --- Setup ---
        TripRepository tripRepo = mock(TripRepository.class);
        StationRepository stationRepo = mock(StationRepository.class);
        DockRepository dockRepo = mock(DockRepository.class);
        UC4_ReturnBikeUseCase sut = new UC4_ReturnBikeUseCase(tripRepo, stationRepo, dockRepo, null);

        DockingStation station = new DockingStation();
        station.setId(200L);
        Dock emptyDock = new Dock();
        emptyDock.setId(300L);
        emptyDock.setStatus(DockStatus.EMPTY);
        station.setDocks(new ArrayList<>(List.of(emptyDock)));

        Bike bike = spy(new Bike());
        bike.setId(42L);

        Trip trip = new Trip();
        trip.setId(1000L);
        trip.setBike(bike);
        trip.setStatus(TripStatus.STARTED);
        trip.setStartTime(LocalDateTime.now().minusMinutes(5));

        when(tripRepo.findById(1000L)).thenReturn(Optional.of(trip));
        when(stationRepo.findById(200L)).thenReturn(Optional.of(station));
        doReturn(true).when(bike).returnBike(emptyDock);

        when(dockRepo.save(any(Dock.class))).thenAnswer(i -> i.getArgument(0));
        when(tripRepo.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

        // --- BEFORE STATE ---
        System.out.println("[Before]");
        System.out.println("   Trip status: " + trip.getStatus());
        System.out.println("   Dock status: " + emptyDock.getStatus());
        System.out.println("   Bike dock: " + bike.getDock());

        // --- ACTION ---
        System.out.println("\n[Action] Returning bike...");
        ReturnBikeResponse resp = sut.execute(new ReturnBikeRequest(1000L, 200L));

        // --- AFTER STATE ---
        System.out.println("\n[After]");
        System.out.println("   Trip status: " + trip.getStatus());
        System.out.println("   Dock status: " + emptyDock.getStatus());
        System.out.println("   Bike dock now: " + bike.getDock());
        System.out.println("   Trip end station: " + trip.getEndStation().getId());
        System.out.println("   Response → trip=" + resp.tripId() + ", bike=" + resp.bikeId() +
                ", station=" + resp.endStationId() + ", status=" + resp.status());

        // --- ASSERTIONS ---
        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(emptyDock.getStatus()).isEqualTo(DockStatus.OCCUPIED);
        assertThat(emptyDock.getBike()).isEqualTo(bike);
        assertThat(resp.status()).isEqualTo("COMPLETED");

        verify(dockRepo).save(emptyDock);
        verify(tripRepo).save(trip);
    }
}
