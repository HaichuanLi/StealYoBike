package com.acme.bms.api.rider;

public record StationSubscriptionResponse(
    Long stationId,
    String stationName,
    String address,
    int availableBikes,
    int capacity,
    double availabilityPercentage
) {}
