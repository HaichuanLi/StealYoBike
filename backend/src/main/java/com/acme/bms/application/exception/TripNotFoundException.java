package com.acme.bms.application.exception;

public class TripNotFoundException extends RuntimeException {
    public TripNotFoundException(Long tripId) {
        super("Trip not found: " + tripId);
    }
}
