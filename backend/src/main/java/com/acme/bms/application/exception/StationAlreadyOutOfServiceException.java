package com.acme.bms.application.exception;

public class StationAlreadyOutOfServiceException extends RuntimeException {
    public StationAlreadyOutOfServiceException(Long stationId) {
        super("Station " + stationId + " is already out of service");
    }
}
