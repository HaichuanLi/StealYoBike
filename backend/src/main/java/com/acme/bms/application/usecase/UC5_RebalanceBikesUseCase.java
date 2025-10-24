package com.acme.bms.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.acme.bms.api.operator.RebalanceRequest;
import com.acme.bms.api.operator.RebalanceResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.repo.DockRepository;
import com.acme.bms.domain.repo.StationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC5_RebalanceBikesUseCase {

    private final StationRepository stationRepo;
    private final DockRepository dockRepo;

    @Transactional
    public RebalanceResponse execute(RebalanceRequest req) {
        DockingStation from = stationRepo.findById(req.fromStationId())
                .orElseThrow(() -> new IllegalArgumentException("From-station not found: " + req.fromStationId()));
        DockingStation to = stationRepo.findById(req.toStationId())
                .orElseThrow(() -> new IllegalArgumentException("To-station not found: " + req.toStationId()));

        int moved = 0, target = Math.max(0, req.count());
        for (int i = 0; i < target; i++) {
            Bike bike = from.getFirstAvailableBike(req.bikeType());
            if (bike == null) break; // no more bikes of that type
            Dock targetDock = to.findEmptyDock();
            if (targetDock == null) break; // no empty docks left
            Dock sourceDock = bike.getDock();
            if (sourceDock == null) break; // inconsistent state

            targetDock.setBike(bike);
            targetDock.setStatus(DockStatus.OCCUPIED);
            sourceDock.setBike(null);
            sourceDock.setStatus(DockStatus.EMPTY);
            bike.setDock(targetDock);

            dockRepo.save(sourceDock);
            dockRepo.save(targetDock);
            moved++;
        }
        return new RebalanceResponse(moved, req.fromStationId(), req.toStationId());
    }
}
