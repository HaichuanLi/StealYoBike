package com.acme.bms.api.operator;

public record RebalanceResponse(
        int moved,
        Long fromStationId,
        Long toStationId
) {}
