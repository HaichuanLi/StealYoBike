package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.acme.bms.api.operator.RestoreInitialStateRequest;
import com.acme.bms.api.operator.RestoreInitialStateResponse;
import com.acme.bms.application.events.SystemRestoredEvent;
import com.acme.bms.application.exception.ForbiddenOperationException;
import com.acme.bms.application.exception.OperatorNotFoundException;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.UserRepository;

class UC8Test {

    @Test
    void execute_restoresSystem_andPublishesEvent() {
        System.out.println("\n=== UC8: Happy path — restore initial system state ===");

        // --- Mocks and SUT ---
        UserRepository userRepository = mock(UserRepository.class);
        StationRepository stationRepository = mock(StationRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        UC8_RestoreInitialStateUseCase useCase =
                new UC8_RestoreInitialStateUseCase(userRepository, stationRepository, eventPublisher);

        // --- Operator setup ---
        User operatorUser = new User();
        operatorUser.setId(99L);
        operatorUser.setRole(Role.OPERATOR);
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(operatorUser));

        // --- Domain setup: two stations, two docks each ---
        DockingStation stationA = new DockingStation(); stationA.setId(1L);
        DockingStation stationB = new DockingStation(); stationB.setId(2L);

        Dock dockA1 = new Dock(); dockA1.setId(11L);
        Dock dockA2 = new Dock(); dockA2.setId(12L);
        Dock dockB1 = new Dock(); dockB1.setId(21L);
        Dock dockB2 = new Dock(); dockB2.setId(22L);

        Bike bikeA = new Bike(); bikeA.setId(1001L);
        Bike bikeB = new Bike(); bikeB.setId(1002L);

        // Station A: Dock A1 has bikeA; Dock A2 is empty
        dockA1.setBike(bikeA); // bikeA.dock is not set on purpose (will be fixed by UC8)
        // Station B: Dock B1 has bikeB but with a bad back reference (bikeB.getDock() != dockB1)
        dockB1.setBike(bikeB); // bikeB.dock remains null to simulate inconsistent state
        // Dock B2 empty

        stationA.setDocks(new java.util.ArrayList<>(List.of(dockA1, dockA2)));
        stationB.setDocks(new java.util.ArrayList<>(List.of(dockB1, dockB2)));

        when(stationRepository.findAll()).thenReturn(List.of(stationA, stationB));
        when(stationRepository.save(any(DockingStation.class))).thenAnswer(i -> i.getArgument(0));

        //Before
        System.out.println("[Before Restore]");
        System.out.println("  Total stations before restore: 2");
        System.out.println("  Station A: 2 docks (first has bike, second empty)");
        System.out.println("  Station B: 2 docks (first has bike with bad back reference, second empty)");
        System.out.println("  Dock A1 status: " + dockA1.getStatus() + " | bikeA current dock: " + bikeA.getDock());
        System.out.println("  Dock A2 status: " + dockA2.getStatus());
        System.out.println("  Dock B1 status: " + dockB1.getStatus() + " | bikeB current dock: " + bikeB.getDock());
        System.out.println("  Dock B2 status: " + dockB2.getStatus());

        //action
        System.out.println("\n[Action]");
        System.out.println("Executing UC8: restoring system to initial state for operator ID=99...");
        RestoreInitialStateRequest request = new RestoreInitialStateRequest(99L);
        RestoreInitialStateResponse response = useCase.execute(request);

        //after
        System.out.println("\n[After Restore]");
        System.out.println("  Stations restored: " + response.stations());
        System.out.println("  Docks restored:    " + response.docks());
        System.out.println("  Bikes restored:    " + response.bikes());
        System.out.println("  Message:           " + response.message());
        System.out.println("  Timestamp:         " + response.restoredAt());
        System.out.println("  Dock A1 status: " + dockA1.getStatus() + " | bikeA current dock: " + bikeA.getDock());
        System.out.println("  Dock A2 status: " + dockA2.getStatus());
        System.out.println("  Dock B1 status: " + dockB1.getStatus() + " | bikeB current dock: " + bikeB.getDock());
        System.out.println("  Dock B2 status: " + dockB2.getStatus());
        System.out.println("[Result] All docks and bikes restored to consistent initial configuration.");

        //Counts
        assertThat(response.stations()).isEqualTo(2);
        assertThat(response.docks()).isEqualTo(4);
        assertThat(response.bikes()).isEqualTo(2);

        //Status & Backref
        assertThat(dockA1.getStatus()).isEqualTo(DockStatus.OCCUPIED);
        assertThat(dockA2.getStatus()).isEqualTo(DockStatus.EMPTY);
        assertThat(dockB1.getStatus()).isEqualTo(DockStatus.OCCUPIED);
        assertThat(dockB2.getStatus()).isEqualTo(DockStatus.EMPTY);

        assertThat(bikeA.getDock()).isEqualTo(dockA1);
        assertThat(bikeB.getDock()).isEqualTo(dockB1);

        // --- EVENT published ---
        verify(eventPublisher).publishEvent(isA(SystemRestoredEvent.class));
        System.out.println("[Success] UC8 restored state; counts/statuses/backrefs verified.\n");
    }

    @Test
    void execute_throws_whenOperatorMissing() {
        System.out.println("\n=== UC8: Error path — operator not found ===");

        UserRepository userRepository = mock(UserRepository.class);
        StationRepository stationRepository = mock(StationRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        UC8_RestoreInitialStateUseCase useCase =
                new UC8_RestoreInitialStateUseCase(userRepository, stationRepository, eventPublisher);

        when(userRepository.findById(404L)).thenReturn(java.util.Optional.empty());

        System.out.println("[Before] No user exists for operatorId=404");
        RestoreInitialStateRequest request = new RestoreInitialStateRequest(404L);

        System.out.println("[Action] execute() expecting OperatorNotFoundException...");
        assertThrows(OperatorNotFoundException.class, () -> useCase.execute(request));

        verifyNoInteractions(stationRepository, eventPublisher);
        System.out.println("[Success] OperatorNotFoundException thrown; no state changes performed.\n");
    }

    @Test
    void execute_throws_whenUserIsNotOperator() {
        System.out.println("\n=== UC8: Error path — user is not an operator ===");

        UserRepository userRepository = mock(UserRepository.class);
        StationRepository stationRepository = mock(StationRepository.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        UC8_RestoreInitialStateUseCase useCase =
                new UC8_RestoreInitialStateUseCase(userRepository, stationRepository, eventPublisher);

        User riderUser = new User(); riderUser.setId(12L); riderUser.setRole(Role.RIDER);
        when(userRepository.findById(12L)).thenReturn(java.util.Optional.of(riderUser));

        System.out.println("[Before] User 12 exists but role=RIDER");
        RestoreInitialStateRequest request = new RestoreInitialStateRequest(12L);

        System.out.println("[Action] execute() expecting ForbiddenOperationException...");
        assertThrows(ForbiddenOperationException.class, () -> useCase.execute(request));

        verifyNoInteractions(stationRepository, eventPublisher);
        System.out.println("[Success] ForbiddenOperationException thrown; no state changes performed.\n");
    }
}
