package com.acme.bms.application.events;

public record BikeCheckedOutEvent(Long tripId, Long userId, Long bikeId, Long stationId) {
}
