package com.acme.bms.application.exception;

public class BikeReturnFailedException extends RuntimeException {
    public BikeReturnFailedException(Long bikeId, Long dockId) {
        super("Bike " + bikeId + " could not be returned to dock " + dockId);
    }
}
