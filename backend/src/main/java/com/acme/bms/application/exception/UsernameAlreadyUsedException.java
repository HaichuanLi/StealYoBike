package com.acme.bms.application.exception;

public class UsernameAlreadyUsedException extends RuntimeException {
    public UsernameAlreadyUsedException() { super("Username already in use"); }
}
