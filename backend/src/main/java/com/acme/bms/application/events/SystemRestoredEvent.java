package com.acme.bms.application.events;

import java.time.Instant;

public record SystemRestoredEvent(
                Long operatorId,
                int stations,
                int docks,
                int bikes,
                Instant occurredAt) {
}
