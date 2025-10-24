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
        System.out.println("UC6 TEST START: Operator marks station OUT_OF_SERVICE");

        // Setup mocks
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
        System.out.println("Loaded operator (ID=" + operator.getId() + ", Role=" + operator.getRole() + ")");

        // Mock station with 2 docks
        DockingStation station = new DockingStation();
        station.setId(10L);
        station.setStatus(StationStatus.ACTIVE); // current operational state
        System.out.println("Initial station status: " + station.getStatus());

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
        System.out.println("Station has " + docks.size() + " docks before change.");

        // Execute usecase
        ChangeStationStateRequest request = new ChangeStationStateRequest(1L, 10L);
        System.out.println("Executing UC6: marking station OUT_OF_SERVICE...");
        ChangeStationStateResponse response = sut.execute(request);

        // Verify results
        System.out.println("Response received:");
        System.out.println("  Station ID: " + response.stationId());
        System.out.println("  New status: " + response.status());
        System.out.println("Verifying internal state updates...");

        assertThat(response.stationId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(StationStatus.OUT_OF_SERVICE);
        assertThat(station.getStatus()).isEqualTo(StationStatus.OUT_OF_SERVICE);

        // Docks should now be OUT_OF_SERVICE
        assertThat(dock1.getStatus()).isEqualTo(DockStatus.OUT_OF_SERVICE);
        assertThat(dock2.getStatus()).isEqualTo(DockStatus.OUT_OF_SERVICE);
        System.out.println("All docks successfully marked OUT_OF_SERVICE.");

        // Bike should now be in MaintenanceState
        verify(bike).setState(isA(com.acme.bms.domain.entity.Status.BikeStrategy.MaintenanceState.class));
        System.out.println("Bike state set to MaintenanceState.");

        // Station saved and event published
        verify(stationRepo).save(station);
        verify(publisher).publishEvent(isA(ChangeStationStatusEvent.class));
        System.out.println("Station changes persisted and event published successfully.");

        System.out.println("UC6 TEST PASSED: Station and assets correctly marked OUT_OF_SERVICE");
    }
}
