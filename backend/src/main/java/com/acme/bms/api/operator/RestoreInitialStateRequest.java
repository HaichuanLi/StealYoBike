package com.acme.bms.api.operator;

import jakarta.validation.constraints.NotNull;

public record RestoreInitialStateRequest(
        @NotNull Long operatorId
) {}
