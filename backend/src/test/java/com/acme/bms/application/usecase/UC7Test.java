package com.acme.bms.application.usecase;


import com.acme.bms.api.operator.OperatorSendBikeToMaintenanceRequest;
import com.acme.bms.api.operator.OperatorSendBikeToMaintenanceResponse;
import com.acme.bms.application.events.OperatorSendBikeToMaintenanceEvent;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Status.BikeState.AvailableState;
import com.acme.bms.domain.entity.Status.BikeState.MaintenanceState;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.BikeRepository;
import com.acme.bms.domain.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

class UC7Test {

    @Test
    void execute_sendsBikeToMaintenance_and_publishesEvent() {
        System.out.println("UC7 TEST: Operator sends bike to maintenance");

        // Mock dependencies
        BikeRepository bikeRepo = mock(BikeRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        UC7_OperatorSendBikeToMaintenance sut =
                new UC7_OperatorSendBikeToMaintenance(bikeRepo, userRepo, publisher);

        // Mock operator
        User operator = new User();
        operator.setId(1L);
        operator.setRole(Role.OPERATOR);
        when(userRepo.findById(1L)).thenReturn(Optional.of(operator));

        // Mock bike
        Bike bike = spy(new Bike());
        bike.setId(100L);
        bike.setState(new AvailableState(bike)); // bike initially available

        when(bikeRepo.findById(100L)).thenReturn(Optional.of(bike));
        when(bikeRepo.save(bike)).thenReturn(bike);

        // Log preconditions
        System.out.println("Before execution:");
        System.out.println("  Bike ID: " + bike.getId() + " | State: " + bike.getState());

        // Execute use case
        OperatorSendBikeToMaintenanceRequest request = new OperatorSendBikeToMaintenanceRequest(1L, 100L, null);
        OperatorSendBikeToMaintenanceResponse response = sut.execute(request);

        // Log after execution
        System.out.println("\nAfter execution:");
        System.out.println("  Bike ID: " + bike.getId() + " | State: " + bike.getState());
        System.out.println("  Response -> Bike ID: " + response.bikeId() + ", State: " + response.state());

        // Verify
        assertThat(response.bikeId()).isEqualTo(100L);
        assertThat(response.state()).isEqualTo("Maintenance");
        assertThat(bike.getState()).isInstanceOf(MaintenanceState.class);
        verify(bikeRepo).save(bike);
        verify(publisher).publishEvent(isA(OperatorSendBikeToMaintenanceEvent.class));
        System.out.println("\nTest passed: Bike correctly transitioned to Maintenance state.");
    }
}