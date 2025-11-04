package com.acme.bms.api.operator;

import java.time.Instant;

public record RestoreInitialStateResponse(
        int stations,
        int docks,
        int bikes,
        String message,
        Instant restoredAt
) {}
