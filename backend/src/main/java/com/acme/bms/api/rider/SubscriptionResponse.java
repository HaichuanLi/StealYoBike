package com.acme.bms.api.rider;

 // Response DTOs
 public record SubscriptionResponse(
    Long stationId,
    String stationName,
    boolean subscribed,
    String message
) {}
