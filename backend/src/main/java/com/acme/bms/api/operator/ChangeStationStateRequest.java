package com.acme.bms.api.operator;

import com.acme.bms.domain.entity.Status.StationStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeStationStateRequest(
    @NotNull Long operatorId,
    @NotNull Long bikeId,
    @NotNull StationStatus state
) {}