package com.acme.bms.application.usecase;

import com.acme.bms.api.operator.RebalanceRequest;
import com.acme.bms.api.operator.RebalanceResponse;
import com.acme.bms.domain.entity.*;
import com.acme.bms.domain.repo.DockRepository;
import com.acme.bms.domain.repo.StationRepository;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UC5Test {

    @Test
    void execute_movesExactCount_whenBikesAndDocksAvailable() {
        System.out.println("\n=== UC5: Rebalance Bikes Use Case ===");

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

        Dock t1 = mock(Dock.class);
        Dock t2 = mock(Dock.class);
        when(from.getFirstAvailableBike(BikeType.ELECTRIC)).thenReturn(b1).thenReturn(b2);
        when(to.findEmptyDock()).thenReturn(t1).thenReturn(t2);

        System.out.println("[Before]");
        System.out.println("   Source docks: 2 bikes available.");
        System.out.println("   Destination docks: 2 empty slots.");

        System.out.println("\n[Action] Rebalancing 2 bikes from Station 1 → Station 2...");
        RebalanceRequest req = new RebalanceRequest(1L, 2L, BikeType.ELECTRIC, 2);
        RebalanceResponse resp = sut.execute(req);

        System.out.println("\n[After]");
        System.out.println("   Bikes moved: " + resp.moved());
        System.out.println("   From station: " + resp.fromStationId());
        System.out.println("   To station: " + resp.toStationId());

        assertThat(resp.moved()).isEqualTo(2);
        verify(t1).setBike(b1);
        verify(t2).setBike(b2);
        verify(s1).setBike(null);
        verify(s2).setBike(null);
        verify(b1).setDock(t1);
        verify(b2).setDock(t2);

        System.out.println("[Success] Both bikes moved successfully.\n");
    }

    @Test
    void execute_stopsEarly_whenDestinationRunsOutOfEmptyDocks() {
        System.out.println("\n=== UC5: Early stop — destination full ===");

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

        System.out.println("[Before]");
        System.out.println("   Source docks: 2 bikes available.");
        System.out.println("   Destination: only 1 empty dock.");

        System.out.println("\n[Action] Attempting to move 2 bikes...");
        RebalanceRequest req = new RebalanceRequest(1L, 2L, BikeType.REGULAR, 2);
        RebalanceResponse resp = sut.execute(req);

        System.out.println("\n[After]");
        System.out.println("   Bikes successfully moved: " + resp.moved());
        System.out.println("   Expected early stop due to no empty docks left.");

        assertThat(resp.moved()).isEqualTo(1);
        verify(t1).setBike(b1);
        verify(s1).setBike(null);
        verify(b1).setDock(t1);

        System.out.println("[Success] Early stop verified logically.\n");
    }
}
