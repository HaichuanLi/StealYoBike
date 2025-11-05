package com.acme.bms.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.operator.RebalanceRequest;
import com.acme.bms.api.operator.RebalanceResponse;
import com.acme.bms.application.exception.NoAvailableBikesException;
import com.acme.bms.application.exception.NoEmptyDockAvailableException;
import com.acme.bms.application.exception.StationNotFoundException;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.repo.DockRepository;
import com.acme.bms.domain.repo.StationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import com.acme.bms.application.events.StationsChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.domain.entity.User;
import com.acme.bms.application.exception.OperatorNotFoundException;
import com.acme.bms.application.exception.ForbiddenOperationException;
import com.acme.bms.domain.entity.Role;

@Service
@RequiredArgsConstructor
public class UC5_RebalanceBikesUseCase {

    private final StationRepository stationRepo;
    private final DockRepository dockRepo;
    private final UserRepository userRepository;
    private ApplicationEventPublisher events;

    @Autowired(required = false)
    public void setEvents(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Transactional
    public RebalanceResponse execute(Long operatorId, RebalanceRequest req) {
        // authN/authZ: validate operator
        User operator = userRepository.findById(operatorId)
                .orElseThrow(OperatorNotFoundException::new);
        if (operator.getRole() != Role.OPERATOR) {
            throw new ForbiddenOperationException("User is not authorized to perform this action");
        }
        DockingStation from = stationRepo.findById(req.fromStationId())
                .orElseThrow(() -> new StationNotFoundException(req.fromStationId()));
        DockingStation to = stationRepo.findById(req.toStationId())
                .orElseThrow(() -> new StationNotFoundException(req.toStationId()));

        int moved = 0;
        int target = Math.max(0, req.count());

        for (int i = 0; i < target; i++) {
            Bike bike = from.getFirstAvailableBike(req.bikeType());
            if (bike == null) {
                if (moved == 0)
                    throw new NoAvailableBikesException(req.fromStationId(), req.bikeType().name());
                break; // stop if none left; partial success is fine
            }
            Dock targetDock = to.findEmptyDock();
            if (targetDock == null) {
                if (moved == 0)
                    throw new NoEmptyDockAvailableException(req.toStationId());
                break; // stop if none left; partial success is fine
            }
            Dock sourceDock = bike.getDock();
            if (sourceDock == null) {
                // Inconsistent state; abort cleanly (partial success already counted).
                break;
            }

            // move bike
            targetDock.setBike(bike);
            targetDock.setStatus(DockStatus.OCCUPIED);
            sourceDock.setBike(null);
            sourceDock.setStatus(DockStatus.EMPTY);
            bike.setDock(targetDock);

            dockRepo.save(sourceDock);
            dockRepo.save(targetDock);
            moved++;
        }

        // notify listeners to broadcast updated snapshot
        if (events != null) {
            events.publishEvent(new StationsChangedEvent());
        }

        return new RebalanceResponse(moved, req.fromStationId(), req.toStationId());
    }
}
