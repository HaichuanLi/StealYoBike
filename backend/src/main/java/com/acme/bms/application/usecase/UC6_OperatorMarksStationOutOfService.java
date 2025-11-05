package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.acme.bms.api.operator.ChangeStationStateRequest;
import com.acme.bms.api.operator.ChangeStationStateResponse;
import com.acme.bms.application.events.ChangeStationStatusEvent;
import com.acme.bms.application.exception.StationNotFoundException;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.repo.StationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC6_OperatorMarksStationOutOfService {

    private final StationRepository stationRepository;
    private final ApplicationEventPublisher events;

    @Transactional
    public ChangeStationStateResponse execute(ChangeStationStateRequest request) {
        DockingStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new StationNotFoundException(request.stationId()));

        // Toggle behavior: if currently OUT_OF_SERVICE -> set ACTIVE (normalize docks)
        // otherwise set OUT_OF_SERVICE (and send bikes to maintenance)
        if (station.getStatus() == StationStatus.OUT_OF_SERVICE) {
            // switch to ACTIVE and normalize docks based on bike presence
            station.setStatus(StationStatus.ACTIVE);
            station.getDocks().forEach(dock -> {
                if (dock.getBike() != null) {
                    // ensure associations consistent
                    if (dock.getBike().getDock() != dock) {
                        dock.getBike().setDock(dock);
                    }
                    // if the bike was forced into maintenance by the operator when the station
                    // was taken out of service, restore it to AVAILABLE
                    if (dock.getBike().isMaintenanceForced()) {
                        dock.getBike().setStatus(com.acme.bms.domain.entity.Status.BikeStatus.AVAILABLE);
                        dock.getBike().setState(
                                new com.acme.bms.domain.entity.Status.BikeState.AvailableState(dock.getBike()));
                        dock.getBike().setMaintenanceForced(false);
                    }
                    dock.setStatus(DockStatus.OCCUPIED);
                } else {
                    dock.setStatus(DockStatus.EMPTY);
                }
            });
        } else {
            // mark station + docks/bikes out of service
            station.setStatus(StationStatus.OUT_OF_SERVICE);
            station.getDocks().forEach(dock -> {
                dock.setStatus(DockStatus.OUT_OF_SERVICE);
                if (dock.getBike() != null) {
                    dock.getBike().sendToMaintenance();
                }
            });
        }

        DockingStation saved = stationRepository.save(station);
        events.publishEvent(new ChangeStationStatusEvent(saved.getId(), saved.getStatus()));

        return new ChangeStationStateResponse(saved.getId(), saved.getStatus());
    }
}
