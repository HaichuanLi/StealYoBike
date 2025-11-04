package com.acme.bms.api.operator;

import com.acme.bms.domain.entity.Status.BikeState.BikeState;

import jakarta.validation.constraints.NotNull;

public record OperatorSendBikeToMaintenanceRequest(
    @NotNull Long operatorId, 
    @NotNull Long bikeId, 
    @NotNull BikeState state
){}

    

