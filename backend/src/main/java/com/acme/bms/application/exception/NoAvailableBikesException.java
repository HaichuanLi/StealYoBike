package com.acme.bms.application.exception;

public class NoAvailableBikesException extends RuntimeException {
    public NoAvailableBikesException(Long stationId, String bikeType) {
        super("No available bikes of type " + bikeType + " at station " + stationId);
    }
}
