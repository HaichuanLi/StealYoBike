package com.acme.bms.application.exception;

public class StationNotFoundException extends RuntimeException {
    public StationNotFoundException(Long stationId) {
        super("Station not found: " + stationId);
    }
}
