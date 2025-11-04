package com.acme.bms.application.exception;

public class OperatorNotFoundException extends RuntimeException {
    public OperatorNotFoundException() {
        super("Operator not found");
    }
}
