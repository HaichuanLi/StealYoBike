package com.acme.bms.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.api.rider.ReserveBikeRequest;
import com.acme.bms.api.rider.ReserveBikeResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.application.events.BikeReservedEvent;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.domain.entity.User;
import com.acme.bms.api.rider.ReservationInfoResponse;
import com.acme.bms.api.rider.ReservationCancelResponse;

import com.acme.bms.application.exception.StationNotFoundException;
import com.acme.bms.application.exception.NoAvailableBikesException;
import com.acme.bms.application.exception.UserNotFoundException;
import com.acme.bms.application.exception.ReservationNotFoundException;
import com.acme.bms.application.exception.ActiveReservationOrTripExistsException;

import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class UC3_ReserveCheckoutUseCase {
    private final ReservationRepository reservationRepository;
    private final TripRepository tripRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReserveBikeResponse execute(ReserveBikeRequest request, Long riderId) {
        if (riderId == null) {
            throw new UserNotFoundException(); // treated as 401/404 by handler
        }

        User rider = userRepository.findById(riderId)
                .orElseThrow(UserNotFoundException::new);

        // Check if rider already has an active reservation
        Reservation existingReservation = reservationRepository.findByRiderIdAndStatus(riderId,
                com.acme.bms.domain.entity.Status.ReservationStatus.ACTIVE);
        if (existingReservation != null) {
            throw new ActiveReservationOrTripExistsException();
        }

        // Check if rider already has an active trip
        Trip existingTrip = tripRepository.findByRiderIdAndStatus(riderId,
                com.acme.bms.domain.entity.Status.TripStatus.STARTED);
        if (existingTrip != null) {
            throw new ActiveReservationOrTripExistsException();
        }

        DockingStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new StationNotFoundException(request.stationId()));

        Bike bike = station.getFirstAvailableBike(request.bikeType());
        if (bike == null) {
            throw new NoAvailableBikesException(request.stationId(), request.bikeType().name());
        }

        Reservation reservation = new Reservation(rider, bike);
        reservationRepository.save(reservation);

        eventPublisher.publishEvent(new BikeReservedEvent(reservation.getId(), rider.getId(), bike.getId()));

        return new ReserveBikeResponse(
                reservation.getId(),
                bike.getId(),
                station.getId(),
                reservation.getPin(),
                reservation.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public ReservationInfoResponse getCurrentReservation(Long riderId) {
        Reservation reservation = reservationRepository.findByRiderIdAndStatus(riderId,
                com.acme.bms.domain.entity.Status.ReservationStatus.ACTIVE);
        if (reservation == null) {
            throw new ReservationNotFoundException();
        }
        return new ReservationInfoResponse(reservation);
    }

    @Transactional
    public ReservationCancelResponse cancelCurrentReservation(Long riderId) {

        if (riderId == null) {
            throw new UserNotFoundException();
        }

        userRepository.findById(riderId)
                .orElseThrow(UserNotFoundException::new);

        Reservation reservation = reservationRepository.findByRiderIdAndStatus(riderId,
                com.acme.bms.domain.entity.Status.ReservationStatus.ACTIVE);
        if (reservation == null) {
            throw new ReservationNotFoundException();
        }
        reservation.cancelReservation();
        reservationRepository.save(reservation);
        return new ReservationCancelResponse(reservation.getId(), "Reservation cancelled successfully.");
    }
}
