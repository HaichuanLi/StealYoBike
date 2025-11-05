package com.acme.bms.application.usecase;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.rider.ReturnBikeRequest;
import com.acme.bms.api.rider.ReturnBikeResponse;
import com.acme.bms.application.exception.BikeReturnFailedException;
import com.acme.bms.application.exception.NoEmptyDockAvailableException;
import com.acme.bms.application.exception.StationNotFoundException;
import com.acme.bms.application.exception.TripNotActiveException;
import com.acme.bms.application.exception.TripNotFoundException;
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
import org.springframework.context.ApplicationEventPublisher;
import com.acme.bms.application.events.StationsChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
public class UC4_ReturnBikeUseCase {

    private final TripRepository tripRepo;
    private final StationRepository stationRepo;
    private final DockRepository dockRepo;
    private ApplicationEventPublisher events;

    @Autowired(required = false)
    public void setEvents(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Transactional
    public ReturnBikeResponse execute(ReturnBikeRequest req) {
        // 1) Trip must exist and be active
        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new TripNotFoundException(req.tripId()));

        if (trip.getStatus() != TripStatus.STARTED) {
            throw new TripNotActiveException(req.tripId());
        }

        // 2) Target station must exist
        DockingStation station = stationRepo.findById(req.stationId())
                .orElseThrow(() -> new StationNotFoundException(req.stationId()));

        // 3) Find an empty dock
        Dock emptyDock = station.getDocks().stream()
                .filter(d -> d.getStatus() == DockStatus.EMPTY)
                .findFirst()
                .orElseThrow(() -> new NoEmptyDockAvailableException(req.stationId()));

        // 4) Return via strategy
        Bike bike = trip.getBike();
        boolean returned = bike.returnBike(emptyDock);
        if (!returned) {
            throw new BikeReturnFailedException(bike.getId(), emptyDock.getId());
        }

        // 5) Update dock/bike state if strategy didn’t already
        if (emptyDock.getBike() == null)
            emptyDock.setBike(bike);
        if (emptyDock.getStatus() != DockStatus.OCCUPIED)
            emptyDock.setStatus(DockStatus.OCCUPIED);
        if (bike.getDock() != emptyDock)
            bike.setDock(emptyDock);

        // 6) Close trip
        trip.setEndStation(station);
        trip.setEndTime(LocalDateTime.now());
        trip.setStatus(TripStatus.COMPLETED);

        // 7) Persist
        dockRepo.save(emptyDock);
        tripRepo.save(trip);

        // publish a stations-changed event so SSE listeners can be updated (if
        // publisher present)
        if (events != null) {
            events.publishEvent(new StationsChangedEvent());
        }

        // 8) Response (priceCents placeholder = 0)
        return new ReturnBikeResponse(
                trip.getId(),
                bike.getId(),
                station.getId(),
                trip.getEndTime(),
                0,
                trip.getStatus().name());
    }
}
