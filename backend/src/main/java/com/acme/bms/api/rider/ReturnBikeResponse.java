package com.acme.bms.api.rider;

import java.time.Instant;

public record ReturnBikeResponse(
        Long tripId,
        Long bikeId,
        Long endStationId,
        Instant endTime,
        int priceCents,
        String status 
) {}
