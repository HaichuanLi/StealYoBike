package com.acme.bms.application.usecase;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.api.rider.ReserveBikeRequest;
import com.acme.bms.api.rider.ReserveBikeResponse;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.Reservation;
import org.springframework.context.ApplicationEventPublisher;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.application.events.BikeReservedEvent;

@Service
@RequiredArgsConstructor
public class UC3_ReserveCheckoutUseCase {
    private final ReservationRepository reservationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StationRepository stationRepository;

    public ReserveBikeResponse execute(ReserveBikeRequest request) {
        DockingStation station = stationRepository.findById(request.stationId())
                .orElseThrow(() -> new IllegalArgumentException("Station not found"));
        Bike bike = station.getFirstAvailableBike(request.bikeType());
        if (bike == null) {
            throw new IllegalArgumentException("No available bikes of the requested type at this station");
        }
        Reservation reservation = new Reservation(/* User is fetched from context */ null, bike);
        reservationRepository.save(reservation);
        eventPublisher
                .publishEvent(new BikeReservedEvent(reservation.getId(), null, bike.getId()));
        return new ReserveBikeResponse(reservation.getId(), bike.getId(), station.getId(), reservation.getPin(),
                reservation.getExpiresAt());

    }
}
