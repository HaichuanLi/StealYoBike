package com.acme.bms.api.operator;

import jakarta.validation.constraints.NotNull;

public record ChangeStationStateRequest(
        @NotNull Long operatorId, 
        @NotNull Long stationId){}