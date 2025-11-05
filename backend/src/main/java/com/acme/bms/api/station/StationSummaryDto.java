package com.acme.bms.api.station;

import com.acme.bms.domain.entity.Status.StationStatus;

/**
 * DTO used as a projection target for repository queries. Contains precomputed
 * counts to avoid fetching lazy collections.
 */
public class StationSummaryDto {

    public final Long stationId;
    public final String name;
    public final StationStatus status;
    public final Double latitude;
    public final Double longitude;
    public final String streetAddress;
    public final Long availableBikes; // SUM returns Long
    public final Long availableDocks; // SUM returns Long
    public final int capacity;

    public StationSummaryDto(Long stationId, String name, StationStatus status, Double latitude, Double longitude,
            String streetAddress, Long availableBikes, Long availableDocks, int capacity) {
        this.stationId = stationId;
        this.name = name;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.availableBikes = availableBikes == null ? 0L : availableBikes;
        this.availableDocks = availableDocks == null ? 0L : availableDocks;
        this.capacity = capacity;
    }

}
