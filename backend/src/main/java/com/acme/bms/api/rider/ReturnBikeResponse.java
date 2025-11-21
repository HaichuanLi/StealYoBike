package com.acme.bms.api.rider;

import java.time.LocalDateTime;

public record ReturnBikeResponse(
                Long tripId,
                Long bikeId,
                Long endStationId,
                LocalDateTime endTime,
                int priceCents,
                String status) {
}
