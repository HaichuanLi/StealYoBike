package com.acme.bms.api.operator;

public record ChangeStationStateResponse(
        Long stationId,
        String state
) {}
