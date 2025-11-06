package com.acme.bms.api.rider;

import java.time.Instant;
import com.acme.bms.domain.entity.BikeType;

public record CheckoutResponse(
                Long tripId,
                Long bikeId,
                BikeType bikeType,
                Long startStationId,
                String startStationName,
                Instant startTime,
                String status) {
}
