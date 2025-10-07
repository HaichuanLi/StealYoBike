package com.acme.bms.api.operator;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RebalanceRequest(
        @NotNull Long fromStationId,
        @NotNull Long toStationId,
        @Min(0) int count
) {}
