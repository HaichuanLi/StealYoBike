package com.acme.bms.api.rider;

import jakarta.validation.constraints.NotNull;

public record ReturnBikeRequest(
                @NotNull Long tripId,
                @NotNull Long stationId) {
}
