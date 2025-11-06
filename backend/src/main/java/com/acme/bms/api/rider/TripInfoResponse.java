package com.acme.bms.api.rider;

import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Trip;
import java.time.Instant;
import java.time.ZoneId;

public record TripInfoResponse(
        Long tripId,
        Long bikeId,
        BikeType bikeType,
        Long startStationId,
        String startStationName,
        Instant startTime,
        String status) {
    public TripInfoResponse(Trip trip) {
        this(
                trip.getId(),
                trip.getBike().getId(),
                trip.getBike().getType(),
                trip.getStartStation().getId(),
                trip.getStartStation().getName(),
                trip.getStartTime().atZone(ZoneId.systemDefault()).toInstant(),
                trip.getStatus().name());
    }
}
