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
import com.acme.bms.application.service.StationObserverService;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import com.acme.bms.application.events.StationsChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import com.acme.bms.domain.repo.UserRepository;
@Service
@RequiredArgsConstructor
public class UC4_ReturnBikeUseCase {

    private final TripRepository tripRepo;
    private final StationRepository stationRepo;
    private final DockRepository dockRepo;
    private final UserRepository userRepo;
    private final StationObserverService observerService;
    private ApplicationEventPublisher events;

    @Autowired(required = false)
    public void setEvents(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Transactional
    public ReturnBikeResponse execute(ReturnBikeRequest req) {

        Trip trip = tripRepo.findById(req.tripId())
                .orElseThrow(() -> new TripNotFoundException(req.tripId()));

        if (trip.getStatus() != TripStatus.STARTED) {
            throw new TripNotActiveException(req.tripId());
        }

        DockingStation station = stationRepo.findById(req.stationId())
                .orElseThrow(() -> new StationNotFoundException(req.stationId()));

        Dock emptyDock = station.getDocks().stream()
                .filter(d -> d.getStatus() == DockStatus.EMPTY)
                .findFirst()
                .orElseThrow(() -> new NoEmptyDockAvailableException(req.stationId()));

        // --- FLEX DOLLAR OCCUPANCY CHECK BEFORE RETURN ---
        int occupiedBefore = (int) station.getDocks().stream()
                .filter(d -> d.getStatus() == DockStatus.OCCUPIED)
                .count();

        int total = station.getDocks().size();
        double fillBefore = total > 0 ? (double) occupiedBefore / total : 0;

        // 4) Return via strategy
        Bike bike = trip.getBike();
        boolean returned = bike.returnBike(emptyDock);
        if (!returned) {
            throw new BikeReturnFailedException(bike.getId(), emptyDock.getId());
        }

        // Ensure the dock/bike states are consistent
        if (emptyDock.getBike() == null)
            emptyDock.setBike(bike);
        if (emptyDock.getStatus() != DockStatus.OCCUPIED)
            emptyDock.setStatus(DockStatus.OCCUPIED);
        if (bike.getDock() != emptyDock)
            bike.setDock(emptyDock);

        // Close trip
        trip.setEndStation(station);
        trip.setEndTime(LocalDateTime.now());
        trip.setStatus(TripStatus.COMPLETED);

        // --- AWARD FLEX DOLLARS ---
        if (trip.getRider() != null && fillBefore < 0.25) {
            trip.getRider().setFlexDollar(trip.getRider().getFlexDollar() + 1.0);
            trip.getRider().setLastFlexDollarEarnedTripId(trip.getId());
            userRepo.save(trip.getRider());
        }

        // Persist changes
        dockRepo.save(emptyDock);
        tripRepo.save(trip);
        observerService.checkAndNotify(station);

        if (events != null) {
            events.publishEvent(new StationsChangedEvent());
        }

        return new ReturnBikeResponse(
                trip.getId(),
                bike.getId(),
                station.getId(),
                trip.getEndTime(),
                0,
                trip.getStatus().name());
    }
}

