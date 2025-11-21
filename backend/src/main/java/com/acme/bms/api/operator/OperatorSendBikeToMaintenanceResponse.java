package com.acme.bms.api.operator;

import jakarta.validation.constraints.NotNull;

public record OperatorSendBikeToMaintenanceResponse(
        @NotNull Long bikeId,
        @NotNull String state) {
}
