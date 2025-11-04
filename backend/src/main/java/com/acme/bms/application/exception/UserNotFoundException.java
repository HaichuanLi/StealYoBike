package com.acme.bms.application.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Authenticated user not found");
    }
}
