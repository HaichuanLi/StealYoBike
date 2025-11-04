package com.acme.bms.application.exception;

public class BikeMaintenanceStateException extends RuntimeException {
    public BikeMaintenanceStateException() {
        super("Bike cannot be sent to maintenance in its current state");
    }
}
