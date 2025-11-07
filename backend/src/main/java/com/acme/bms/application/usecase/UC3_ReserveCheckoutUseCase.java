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
import com.acme.bms.application.events.BikeCheckedOutEvent;
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
import com.acme.bms.api.rider.CheckoutRequest;
import com.acme.bms.api.rider.CheckoutResponse;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.api.rider.TripInfoResponse;
import java.time.LocalDateTime;

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

        // Require a payment method on file before allowing reservations/checkouts
        if (rider.getPaymentToken() == null || rider.getPaymentToken().isBlank()) {
            throw new com.acme.bms.application.exception.PaymentMethodRequiredException();
        }

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

    @Transactional
    public CheckoutResponse checkoutBike(CheckoutRequest request, Long riderId) {
        if (riderId == null) {
            throw new UserNotFoundException();
        }

        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ReservationNotFoundException());

        if (!reservation.getRider().getId().equals(riderId)) {
            throw new IllegalStateException("Reservation does not belong to this rider");
        }

        if (reservation.getStatus() != com.acme.bms.domain.entity.Status.ReservationStatus.ACTIVE) {
            throw new IllegalStateException("Reservation is not active");
        }

        if (reservation.isExpired()) {
            throw new IllegalStateException("Reservation has expired");
        }

        if (!reservation.getPin().equals(request.pin())) {
            throw new IllegalArgumentException("Invalid PIN");
        }

        Bike bike = reservation.getBike();
        DockingStation station = bike.getDock().getStation();
        if (!bike.checkoutBike()) {
            throw new IllegalStateException("Bike cannot be checked out");
        }

        Trip trip = Trip.builder()
                .rider(reservation.getRider())
                .bike(bike)
                .startStation(station)
                .startTime(LocalDateTime.now())
                .status(TripStatus.STARTED)
                .priceCents(0)
                .build();

        tripRepository.save(trip);

        reservation.setStatus(com.acme.bms.domain.entity.Status.ReservationStatus.FULFILLED);
        reservationRepository.save(reservation);

        eventPublisher.publishEvent(new BikeCheckedOutEvent(trip.getId(), riderId, bike.getId(), station.getId()));

        return new CheckoutResponse(
                trip.getId(),
                bike.getId(),
                bike.getType(),
                station.getId(),
                station.getName(),
                trip.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant(),
                trip.getStatus().name());
    }

    @Transactional(readOnly = true)
    public TripInfoResponse getCurrentTrip(Long riderId) {
        Trip trip = tripRepository.findByRiderIdAndStatus(riderId, TripStatus.STARTED);
        if (trip == null) {
            return null;
        }
        return new TripInfoResponse(trip);
    }
}
