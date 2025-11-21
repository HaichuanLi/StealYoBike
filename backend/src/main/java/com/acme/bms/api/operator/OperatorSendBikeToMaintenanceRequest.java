package com.acme.bms.api.operator;

import jakarta.validation.constraints.NotNull;

public record OperatorSendBikeToMaintenanceRequest(
        @NotNull Long bikeId) {
}
