package com.acme.bms.api.operator;

import com.acme.bms.domain.entity.Status.DockingStationStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeStationStateResponse(
        @NotNull Long stationId, 
        @NotNull DockingStationStatus status){}