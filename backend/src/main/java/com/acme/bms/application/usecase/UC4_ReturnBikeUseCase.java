package com.acme.bms.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.rider.ReturnBikeRequest;
import com.acme.bms.api.rider.ReturnBikeResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.repo.DockRepository;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC4_ReturnBikeUseCase {

    private final TripRepository tripRepo;
    private final StationRepository stationRepo;
    private final DockRepository dockRepo;

    @Transactional
    public ReturnBikeResponse execute(ReturnBikeRequest req) {
        //Trip must be active
        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        if (trip.getStatus() != TripStatus.STARTED) {
            throw new IllegalStateException("Trip is not active or already completed.");
        }

        //Target station
        DockingStation station = stationRepo.findById(req.stationId())
                .orElseThrow(() -> new IllegalArgumentException("Docking station not found."));

        //Find an empty dock
        Dock emptyDock = station.getDocks().stream()
                .filter(d -> d.getStatus() == DockStatus.EMPTY)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No empty dock available at this station."));

        //Return via strategy
        Bike bike = trip.getBike();
        boolean returned = bike.returnBike(emptyDock);
        if (!returned) {
            throw new IllegalStateException("Bike could not be returned.");
        }

        if (emptyDock.getBike() == null) {
            emptyDock.setBike(bike);
        }
        if (emptyDock.getStatus() != DockStatus.OCCUPIED) {
            emptyDock.setStatus(DockStatus.OCCUPIED);
        }
        if (bike.getDock() != emptyDock) {
            bike.setDock(emptyDock);
        }

        //Close trip
        trip.setEndStation(station);
        trip.setEndTime(LocalDateTime.now());
        trip.setStatus(TripStatus.COMPLETED);

        //Persist
        dockRepo.save(emptyDock);
        tripRepo.save(trip);

        System.out.println("Bike returned (trip " + trip.getId() + ") at station " + station.getId()
                + ", dock " + emptyDock.getId());

        // Return payload (you already defined this DTO)
        return new ReturnBikeResponse(
                trip.getId(),
                bike.getId(),
                station.getId(),
                trip.getEndTime(),
                0,
                trip.getStatus().name()
        );
    }
}
