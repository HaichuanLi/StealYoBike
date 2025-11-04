package com.acme.bms.application.exception;

public class TripNotActiveException extends RuntimeException {
    public TripNotActiveException(Long tripId) {
        super("Trip " + tripId + " is not active or already completed");
    }
}
