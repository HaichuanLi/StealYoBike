package com.acme.bms.api.rider;

public record ReservationInfoResponse(
        Long reservationId,
        Long bikeId,
        Long stationId,
        String pin,
        String expiresAt) {

    public ReservationInfoResponse(com.acme.bms.domain.entity.Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getBike().getId(),
                reservation.getBike().getDock().getStation().getId(),
                reservation.getPin(),
                reservation.getExpiresAt().toString());
    }
}
