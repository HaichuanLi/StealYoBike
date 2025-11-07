package com.acme.bms.api.rider;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PastTripResponse {
    private Long tripId;
    private Long bikeId;
    private String bikeType;
    private String startTime;
    private String endTime;
    private Integer durationMinutes;
    private String startStationName;
    private String endStationName;
    private Double totalAmount;
    private Boolean paid;
    private Long billId;
}
