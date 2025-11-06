package com.acme.bms.application.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException() {
        super("No active reservation found for the user");
    }

}
