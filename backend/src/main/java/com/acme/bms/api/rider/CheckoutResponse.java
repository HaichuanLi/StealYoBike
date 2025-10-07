package com.acme.bms.api.rider;

import java.time.Instant;

public record CheckoutResponse(
        Long tripId,
        Long bikeId,
        Long startStationId,
        Instant startTime,
        String status
) {}
