package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.operator.OperatorSendBikeToMaintenanceRequest;
import com.acme.bms.api.operator.OperatorSendBikeToMaintenanceResponse;
import com.acme.bms.application.events.OperatorSendBikeToMaintenanceEvent;
import com.acme.bms.application.exception.BikeMaintenanceStateException;
import com.acme.bms.application.exception.BikeNotFoundException;
import com.acme.bms.application.exception.ForbiddenOperationException;
import com.acme.bms.application.exception.OperatorNotFoundException;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Status.BikeState.MaintenanceState;
import com.acme.bms.domain.repo.BikeRepository;
import com.acme.bms.domain.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC7_OperatorSendBikeToMaintenance {

    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;

    @Transactional
    public OperatorSendBikeToMaintenanceResponse execute(Long operatorId,
            OperatorSendBikeToMaintenanceRequest request) {
        // authN/authZ: operatorId is provided by controller from Authentication
        User operator = userRepository.findById(operatorId)
                .orElseThrow(OperatorNotFoundException::new);
        if (operator.getRole() != Role.OPERATOR) {
            throw new ForbiddenOperationException("User is not authorized to perform this action");
        }

        // target bike
        Bike bike = bikeRepository.findById(request.bikeId())
                .orElseThrow(() -> new BikeNotFoundException(request.bikeId()));

        // Toggle state transition
        boolean success;
        if (bike.getStatus() == com.acme.bms.domain.entity.Status.BikeStatus.MAINTENANCE) {
            // Activate bike from maintenance
            success = bike.activateFromMaintenance();
        } else {
            // Send bike to maintenance
            success = bike.sendToMaintenance();
            if (success) {
                bike.setState(new MaintenanceState(bike));
            }
        }

        if (!success) {
            throw new BikeMaintenanceStateException();
        }

        Bike savedBike = bikeRepository.save(bike);
        events.publishEvent(new OperatorSendBikeToMaintenanceEvent(operator.getId(), savedBike.getId()));

        return new OperatorSendBikeToMaintenanceResponse(
                savedBike.getId(),
                savedBike.getState().toString());
    }
}
