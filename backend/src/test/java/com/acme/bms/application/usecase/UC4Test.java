package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        //Repos
        TripRepository tripRepo = mock(TripRepository.class);
        StationRepository stationRepo = mock(StationRepository.class);
        DockRepository dockRepo = mock(DockRepository.class);

        UC4_ReturnBikeUseCase sut = new UC4_ReturnBikeUseCase(tripRepo, stationRepo, dockRepo);

        // Domain setup
        DockingStation station = new DockingStation();
        station.setId(200L);

        Dock emptyDock = new Dock();
        emptyDock.setId(300L);
        emptyDock.setStatus(DockStatus.EMPTY);
        emptyDock.setSlotIndex(0);

        List<Dock> docks = new ArrayList<>();
        docks.add(emptyDock);
        station.setDocks(docks);

        Bike bike = spy(new Bike());
        bike.setId(42L);

        Trip trip = new Trip();
        trip.setId(1000L);
        trip.setBike(bike);
        trip.setStatus(TripStatus.STARTED);
        trip.setStartTime(LocalDateTime.now().minusMinutes(10));

        when(tripRepo.findById(1000L)).thenReturn(Optional.of(trip));
        when(stationRepo.findById(200L)).thenReturn(Optional.of(station));

        // Strategy call returns true
        doReturn(true).when(bike).returnBike(emptyDock);

        when(dockRepo.save(any(Dock.class))).thenAnswer(i -> i.getArgument(0));
        when(tripRepo.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ReturnBikeRequest req = new ReturnBikeRequest(1000L, 200L);
        ReturnBikeResponse resp = sut.execute(req);

        assertThat(resp.tripId()).isEqualTo(1000L);
        assertThat(resp.bikeId()).isEqualTo(42L);
        assertThat(resp.endStationId()).isEqualTo(200L);
        assertThat(resp.endTime()).isNotNull();
        assertThat(resp.priceCents()).isEqualTo(0);
        assertThat(resp.status()).isEqualTo("COMPLETED");


        assertThat(emptyDock.getStatus()).isEqualTo(DockStatus.OCCUPIED);
        assertThat(emptyDock.getBike()).isEqualTo(bike);
        assertThat(bike.getDock()).isEqualTo(emptyDock);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
        assertThat(trip.getEndStation()).isEqualTo(station);

        verify(dockRepo).save(emptyDock);
        verify(tripRepo).save(trip);

        System.out.println("UC4 success: trip=" + resp.tripId()
                + ", bike=" + resp.bikeId()
                + ", station=" + resp.endStationId()
                + ", status=" + resp.status());
    }
}
