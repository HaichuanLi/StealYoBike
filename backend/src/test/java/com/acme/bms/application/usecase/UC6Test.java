package com.acme.bms.application.usecase;

import com.acme.bms.api.operator.ChangeStationStateRequest;
import com.acme.bms.api.operator.ChangeStationStateResponse;
import com.acme.bms.application.events.ChangeStationStatusEvent;
import com.acme.bms.domain.entity.*;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

class UC6Test {

    @Test
    void execute_marksStationOutOfService_and_publishesEvent() {
        System.out.println("UC6 TEST: Operator marks station OUT_OF_SERVICE");

        // Mock dependencies
        StationRepository stationRepo = mock(StationRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        UC6_OperatorMarksStationOutOfService sut =
                new UC6_OperatorMarksStationOutOfService(stationRepo, userRepo, publisher);

        // Mock operator
        User operator = new User();
        operator.setId(1L);
        operator.setRole(Role.OPERATOR);
        when(userRepo.findById(1L)).thenReturn(Optional.of(operator));

        // Mock station
        DockingStation station = new DockingStation();
        station.setId(10L);
        station.setStatus(StationStatus.ACTIVE); // before state

        // Mock docks + bike
        Bike bike = mock(Bike.class);
        Dock dock1 = new Dock();
        dock1.setId(100L);
        dock1.setStatus(DockStatus.OCCUPIED);
        dock1.setBike(bike);

        Dock dock2 = new Dock();
        dock2.setId(200L);
        dock2.setStatus(DockStatus.EMPTY);

        List<Dock> docks = new ArrayList<>();
        docks.add(dock1);
        docks.add(dock2);
        station.setDocks(docks);

        when(stationRepo.findById(10L)).thenReturn(Optional.of(station));
        when(stationRepo.save(station)).thenReturn(station);

        // Log the preconditions
        System.out.println("Before execution:");
        System.out.println("  Station ID: " + station.getId() + " | Status: " + station.getStatus());
        System.out.println("  Dock 1 (ID " + dock1.getId() + "): " + dock1.getStatus());
        System.out.println("  Dock 2 (ID " + dock2.getId() + "): " + dock2.getStatus());

        // Execute use case
        ChangeStationStateRequest request = new ChangeStationStateRequest(1L, 10L);
        ChangeStationStateResponse response = sut.execute(request);

        // Log the after-state
        System.out.println("\nAfter execution:");
        System.out.println("  Station ID: " + station.getId() + " | Status: " + station.getStatus());
        System.out.println("  Dock 1 (ID " + dock1.getId() + "): " + dock1.getStatus());
        System.out.println("  Dock 2 (ID " + dock2.getId() + "): " + dock2.getStatus());
        System.out.println("  Response -> Station ID: " + response.stationId() + ", Status: " + response.status());

        // Verify
        assertThat(response.stationId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(StationStatus.OUT_OF_SERVICE);
        assertThat(station.getStatus()).isEqualTo(StationStatus.OUT_OF_SERVICE);
        assertThat(dock1.getStatus()).isEqualTo(DockStatus.OUT_OF_SERVICE);
        assertThat(dock2.getStatus()).isEqualTo(DockStatus.OUT_OF_SERVICE);

        verify(bike).setState(isA(com.acme.bms.domain.entity.Status.BikeState.MaintenanceState.class));
        verify(stationRepo).save(station);
        verify(publisher).publishEvent(isA(ChangeStationStatusEvent.class));

        System.out.println("\nTest passed: Station, docks, and bike correctly transitioned to OUT_OF_SERVICE state.");
    }
}
