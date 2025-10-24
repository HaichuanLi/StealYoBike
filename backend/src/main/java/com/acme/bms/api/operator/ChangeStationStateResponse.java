package com.acme.bms.api.operator;

public record ChangeStationStateResponse(
        Long bikeId, 
        String status
) {}