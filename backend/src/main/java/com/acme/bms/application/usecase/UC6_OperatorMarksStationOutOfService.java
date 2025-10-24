package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.acme.bms.api.operator.ChangeStationStateResponse;
import com.acme.bms.api.operator.ChangeStationStateRequest;
import com.acme.bms.application.events.ChangeStationStatusEvent;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.entity.Status.BikeState.MaintenanceState;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC6_OperatorMarksStationOutOfService {

    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher events;

    @Transactional
    public ChangeStationStateResponse execute(ChangeStationStateRequest request) {
        User operator = userRepository.findById(request.operatorId())
                .orElseThrow(() -> new IllegalArgumentException("Operator not found"));

        if (operator.getRole() != Role.OPERATOR)
            throw new IllegalArgumentException("User is not authorized to perform this action");

        DockingStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new IllegalArgumentException("Docking station not found"));

        if (station.getStatus() == StationStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Station is already out-of-service");
        }

        // Treat any other state (EMPTY, FULL, OCCUPIED) as operational
        station.setStatus(StationStatus.OUT_OF_SERVICE);
        station.getDocks().forEach(dock -> {
            dock.setStatus(DockStatus.OUT_OF_SERVICE);
            if (dock.getBike() != null) {
                dock.getBike().setState(new MaintenanceState(dock.getBike()));
            }
        });

        DockingStation savedStation = stationRepository.save(station);
        events.publishEvent(new ChangeStationStatusEvent(savedStation.getId(), savedStation.getStatus()));

        return new ChangeStationStateResponse(savedStation.getId(), savedStation.getStatus());
    }
}
