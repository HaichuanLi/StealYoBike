package com.acme.bms.api.station;

public record StationListItemResponse(
        Long stationId,
        String name,
        String status,
        Double latitude,
        Double longitude,
        String streetAddress,
        int capacity,
        int bikesDocked,
        int freeDocks,
        int oosDocks,
        int expiresAfterMinutes
) {}
