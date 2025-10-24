package com.acme.bms.application.usecase;

import com.acme.bms.api.operator.RebalanceRequest;
import com.acme.bms.api.operator.RebalanceResponse;
import com.acme.bms.domain.entity.*;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.repo.DockRepository;
import com.acme.bms.domain.repo.StationRepository;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UC5Test {

    @Test
    void execute_movesExactCount_whenBikesAndDocksAvailable() {
        System.out.println("UC5 TEST: Normal rebalancing case");

        StationRepository stations = mock(StationRepository.class);
        DockRepository docks = mock(DockRepository.class);
        UC5_RebalanceBikesUseCase sut = new UC5_RebalanceBikesUseCase(stations, docks);

        DockingStation from = mock(DockingStation.class);
        DockingStation to = mock(DockingStation.class);
        when(stations.findById(1L)).thenReturn(Optional.of(from));
        when(stations.findById(2L)).thenReturn(Optional.of(to));

        Bike b1 = mock(Bike.class);
        Bike b2 = mock(Bike.class);
        Dock s1 = mock(Dock.class);
        Dock s2 = mock(Dock.class);
        when(b1.getDock()).thenReturn(s1);
        when(b2.getDock()).thenReturn(s2);
        when(from.getFirstAvailableBike(BikeType.ELECTRIC)).thenReturn(b1).thenReturn(b2);

        Dock t1 = mock(Dock.class);
        Dock t2 = mock(Dock.class);
        when(to.findEmptyDock()).thenReturn(t1).thenReturn(t2);

        System.out.println("Starting rebalancing of 2 bikes (ELECTRIC) from station 1 to station 2...");
        RebalanceRequest req = new RebalanceRequest(1L, 2L, BikeType.ELECTRIC, 2);
        RebalanceResponse resp = sut.execute(req);
        System.out.println("Rebalancing completed: " + resp.moved() + " bikes moved.");

        assertThat(resp.moved()).isEqualTo(2);
        assertThat(resp.fromStationId()).isEqualTo(1L);
        assertThat(resp.toStationId()).isEqualTo(2L);

        verify(t1).setBike(b1);
        verify(t1).setStatus(DockStatus.OCCUPIED);
        verify(s1).setBike(null);
        verify(s1).setStatus(DockStatus.EMPTY);
        verify(b1).setDock(t1);
        System.out.println("Bike 1 successfully moved from source dock to target dock.");

        verify(t2).setBike(b2);
        verify(t2).setStatus(DockStatus.OCCUPIED);
        verify(s2).setBike(null);
        verify(s2).setStatus(DockStatus.EMPTY);
        verify(b2).setDock(t2);
        System.out.println("Bike 2 successfully moved from source dock to target dock.");

        verify(docks, times(1)).save(s1);
        verify(docks, times(1)).save(t1);
        verify(docks, times(1)).save(s2);
        verify(docks, times(1)).save(t2);
        System.out.println("All docks saved successfully.\n");
    }

    @Test
    void execute_stopsEarly_whenDestinationRunsOutOfEmptyDocks() {
        System.out.println("UC5 TEST: Early stop when destination has no empty docks");

        StationRepository stations = mock(StationRepository.class);
        DockRepository docks = mock(DockRepository.class);
        UC5_RebalanceBikesUseCase sut = new UC5_RebalanceBikesUseCase(stations, docks);

        DockingStation from = mock(DockingStation.class);
        DockingStation to = mock(DockingStation.class);
        when(stations.findById(1L)).thenReturn(Optional.of(from));
        when(stations.findById(2L)).thenReturn(Optional.of(to));

        Bike b1 = mock(Bike.class);
        Bike b2 = mock(Bike.class);
        Dock s1 = mock(Dock.class);
        Dock s2 = mock(Dock.class);
        when(b1.getDock()).thenReturn(s1);
        when(b2.getDock()).thenReturn(s2);
        when(from.getFirstAvailableBike(BikeType.REGULAR)).thenReturn(b1).thenReturn(b2);

        Dock t1 = mock(Dock.class);
        when(to.findEmptyDock()).thenReturn(t1).thenReturn(null);

        System.out.println("Starting rebalancing of 2 bikes (REGULAR) from station 1 to station 2...");
        RebalanceRequest req = new RebalanceRequest(1L, 2L, BikeType.REGULAR, 2);
        RebalanceResponse resp = sut.execute(req);
        System.out.println("Rebalancing stopped early. Total moved: " + resp.moved());

        assertThat(resp.moved()).isEqualTo(1);
        verify(t1).setBike(b1);
        verify(s1).setBike(null);
        verify(b1).setDock(t1);
        verify(docks).save(s1);
        verify(docks).save(t1);
        verifyNoMoreInteractions(docks);
        System.out.println("Verified: one bike moved, second skipped due to lack of space.\n");
    }
}