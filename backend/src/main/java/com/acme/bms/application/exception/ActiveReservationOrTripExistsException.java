package com.acme.bms.application.exception;

public class ActiveReservationOrTripExistsException extends RuntimeException {
    public ActiveReservationOrTripExistsException() {
        super("You already have an active reservation or trip. Please complete or cancel it before making a new reservation.");
    }
}
