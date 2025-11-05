package com.acme.bms.application.usecase;

import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.operator.RestoreInitialStateRequest;
import com.acme.bms.api.operator.RestoreInitialStateResponse;
import com.acme.bms.application.events.SystemRestoredEvent;
import com.acme.bms.application.exception.ForbiddenOperationException;
import com.acme.bms.application.exception.OperatorNotFoundException;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC8_RestoreInitialStateUseCase {

    private final UserRepository users;
    private final StationRepository stations;
    private final ApplicationEventPublisher events;

    @Transactional
    public RestoreInitialStateResponse execute(Long operatorId, RestoreInitialStateRequest req) {
        // valid op - operatorId provided by controller from Authentication
        User operator = users.findById(operatorId)
                .orElseThrow(OperatorNotFoundException::new);
        if (operator.getRole() != Role.OPERATOR) {
            throw new ForbiddenOperationException("Only operators can restore the system.");
        }

        // Normalize system state
        int stationCount = 0;
        int dockCount = 0;
        int bikeCount = 0;

        for (DockingStation station : stations.findAll()) {
            stationCount++;
            for (Dock dock : station.getDocks()) {
                dockCount++;
                Bike bike = dock.getBike();

                if (bike != null) {
                    // ensure associations/flags are consistent
                    if (bike.getDock() != dock) {
                        bike.setDock(dock);
                    }
                    dock.setStatus(DockStatus.OCCUPIED);
                    bikeCount++;
                } else {
                    dock.setStatus(DockStatus.EMPTY);
                }
            }
            stations.save(station); // persist normalization for this station
        }

        // Publish event (for map/dashboard listeners)
        Instant now = Instant.now();
        events.publishEvent(new SystemRestoredEvent(
                operator.getId(),
                stationCount,
                dockCount,
                bikeCount,
                now));

        // Return a short summary for the frontend
        return new RestoreInitialStateResponse(
                stationCount,
                dockCount,
                bikeCount,
                "System restored successfully.",
                now);
    }
}
