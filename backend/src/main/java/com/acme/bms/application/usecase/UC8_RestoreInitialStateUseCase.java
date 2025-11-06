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
import com.acme.bms.domain.entity.Status.BikeStatus;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.repo.BikeRepository;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.domain.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC8_RestoreInitialStateUseCase {

    private final UserRepository users;
    private final StationRepository stations;
    private final BikeRepository bikes;
    private final ReservationRepository reservations;
    private final TripRepository trips;
    private final ApplicationEventPublisher events;

    @Transactional
    public RestoreInitialStateResponse execute(Long operatorId, RestoreInitialStateRequest req) {
        // Validate operator - operatorId provided by controller from Authentication
        User operator = users.findById(operatorId)
                .orElseThrow(OperatorNotFoundException::new);
        if (operator.getRole() != Role.OPERATOR) {
            throw new ForbiddenOperationException("Only operators can restore the system.");
        }

        // Step 1: Cancel all active reservations
        reservations.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.ACTIVE)
                .forEach(r -> {
                    r.setStatus(ReservationStatus.CANCELLED);
                    reservations.save(r);
                });

        // Step 2: Complete all active trips and return bikes to available docks
        trips.findAll().stream()
                .filter(t -> t.getStatus() == TripStatus.STARTED)
                .forEach(t -> {
                    Bike bike = t.getBike();

                    // Find an empty dock to return the bike to
                    if (bike != null && bike.getDock() == null) {
                        // Try to find any empty dock in any active station
                        for (DockingStation station : stations.findAll()) {
                            Dock emptyDock = station.findEmptyDock();
                            if (emptyDock != null) {
                                // Return the bike to this dock (transitions to AVAILABLE)
                                bike.returnBike(emptyDock);
                                emptyDock.setBike(bike);
                                emptyDock.setStatus(DockStatus.OCCUPIED);
                                bike.setDock(emptyDock);

                                // Normalize the bike state
                                bike.setStatus(BikeStatus.AVAILABLE);
                                bike.setState(new com.acme.bms.domain.entity.Status.BikeState.AvailableState(bike));
                                bike.setReservationExpiry(null);
                                bike.setMaintenanceForced(false);

                                // Persist the bike state
                                bikes.save(bike);
                                break; // Found a dock, stop searching
                            }
                        }
                    }

                    t.setStatus(TripStatus.COMPLETED);
                    trips.save(t);
                });

        // Step 3: Normalize system state
        int stationCount = 0;
        int dockCount = 0;
        int bikeCount = 0;

        for (DockingStation station : stations.findAll()) {
            // Restore station to active state
            station.setStatus(StationStatus.ACTIVE);
            stationCount++;

            for (Dock dock : station.getDocks()) {
                dockCount++;
                Bike bike = dock.getBike();

                if (bike != null) {
                    // Ensure bike associations and state are consistent
                    if (bike.getDock() != dock) {
                        bike.setDock(dock);
                    }

                    // Reset bike to available state
                    bike.setStatus(BikeStatus.AVAILABLE);
                    bike.setState(new com.acme.bms.domain.entity.Status.BikeState.AvailableState(bike));
                    bike.setReservationExpiry(null);
                    bike.setMaintenanceForced(false);

                    dock.setStatus(DockStatus.OCCUPIED);
                    bikes.save(bike);
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

        // Return a summary for the frontend
        return new RestoreInitialStateResponse(
                stationCount,
                dockCount,
                bikeCount,
                "System restored successfully.",
                now);
    }
}
