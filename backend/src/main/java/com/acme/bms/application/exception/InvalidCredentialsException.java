package com.acme.bms.application.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("Invalid username, email, or password"); }
}
