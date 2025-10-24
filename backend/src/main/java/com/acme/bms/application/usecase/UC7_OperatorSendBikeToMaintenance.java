package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.acme.bms.application.events.OperatorSendBikeToMaintenanceEvent;
import com.acme.bms.api.operator.ChangeStationStateRequest;
import com.acme.bms.api.operator.ChangeStationStateResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Status.BikeStatus;
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
    public ChangeStationStateResponse execute(ChangeStationStateRequest request){
        User operator = userRepository.findById(request.operatorId())
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        if (operator.getRole() != Role.OPERATOR)
            throw new IllegalArgumentException("User is not authorized to perform this action");
    
        Bike bike = bikeRepository.findById(request.bikeId())
                .orElseThrow(() -> new IllegalArgumentException("Bike not found")); 

        if (bike.getStatus() == BikeStatus.ON_TRIP || bike.getStatus() == BikeStatus.RESERVED) {
            throw new RuntimeException("Cannot send bike to maintenance while it's reserved or on trip");
        }
        bike.setStatus(BikeStatus.MAINTENANCE);
        Bike savedBike = bikeRepository.save(bike);

        events.publishEvent(new OperatorSendBikeToMaintenanceEvent(operator.getId(), savedBike.getId()));

        return new ChangeStationStateResponse(
            savedBike.getId(),
            savedBike.getStatus().name()
        );
    }
}
