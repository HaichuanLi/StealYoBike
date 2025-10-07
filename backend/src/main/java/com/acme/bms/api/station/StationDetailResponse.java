package com.acme.bms.api.station;

import java.util.List;

public record StationDetailResponse(
        Long stationId,
        String name,
        String status,
        Double latitude,
        Double longitude,
        String streetAddress,
        int capacity,
        int expiresAfterMinutes,
        List<DockView> docks
) {
    public record DockView(
            Long dockId,
            String status,
            BikeView bike
    ) {}

    public record BikeView(
            Long bikeId,
            String type,
            String status,
            String reservationExpiry
    ) {}
}
