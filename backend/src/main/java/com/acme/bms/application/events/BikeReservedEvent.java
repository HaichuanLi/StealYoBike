package com.acme.bms.application.events;

public record BikeReservedEvent(Long reservationId, Long userId, Long bikeId) {
}
