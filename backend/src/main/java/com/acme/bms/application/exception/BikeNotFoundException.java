package com.acme.bms.application.exception;

public class BikeNotFoundException extends RuntimeException {
    public BikeNotFoundException(Long bikeId) {
        super("Bike not found: " + bikeId);
    }
}
