package com.acme.bms.application.exception;

public class NoEmptyDockAvailableException extends RuntimeException {
    public NoEmptyDockAvailableException(Long stationId) {
        super("No empty dock available at station " + stationId);
    }
}
