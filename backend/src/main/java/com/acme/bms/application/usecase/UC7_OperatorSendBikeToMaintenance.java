package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.acme.bms.application.events.OperatorSendBikeToMaintenanceEvent;
import com.acme.bms.api.operator.OperatorSendBikeToMaintenanceRequest;
import com.acme.bms.api.operator.OperatorSendBikeToMaintenanceResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Status.BikeState.MaintenanceState;
import com.acme.bms.domain.repo.BikeRepository;
import com.acme.bms.domain.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UC7_OperatorSendBikeToMaintenance {
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;

    
    @Transactional
    public OperatorSendBikeToMaintenanceResponse execute(OperatorSendBikeToMaintenanceRequest request){
        User operator = userRepository.findById(request.operatorId())
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        if (operator.getRole() != Role.OPERATOR)
            throw new IllegalArgumentException("User is not authorized to perform this action");
    
        Bike bike = bikeRepository.findById(request.bikeId())
                .orElseThrow(() -> new IllegalArgumentException("Bike not found")); 

        if (!bike.sendToMaintenance()) {
                throw new RuntimeException("Bike cannot be sent to maintenance in its current state");
            }
        bike.setState(new MaintenanceState(bike));
        Bike savedBike = bikeRepository.save(bike);

        events.publishEvent(new OperatorSendBikeToMaintenanceEvent(operator.getId(), savedBike.getId()));

        return new OperatorSendBikeToMaintenanceResponse(
            savedBike.getId(),
            savedBike.getState().toString()
        );
    }
}
