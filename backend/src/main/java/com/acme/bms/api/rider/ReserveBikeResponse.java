package com.acme.bms.api.rider;

import java.time.Instant;

public record ReserveBikeResponse(
        Long reservationId,
        Long bikeId,
        Long stationId,
        String pin,
        Instant expiresAt
) {}
