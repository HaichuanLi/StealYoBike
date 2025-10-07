package com.acme.bms.api.rider;

import com.acme.bms.domain.entity.BikeType;

import jakarta.validation.constraints.NotNull;

public record ReserveBikeRequest(
        @NotNull Long stationId,
        @NotNull BikeType bikeType
) {}
