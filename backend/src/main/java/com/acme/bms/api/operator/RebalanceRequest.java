package com.acme.bms.api.operator;

import com.acme.bms.domain.entity.BikeType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RebalanceRequest(
                @NotNull Long fromStationId,
                @NotNull Long toStationId,
                @NotNull BikeType bikeType,
                @Min(1) int count) {
}
