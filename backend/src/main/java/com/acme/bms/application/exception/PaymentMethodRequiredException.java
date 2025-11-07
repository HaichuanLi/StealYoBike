package com.acme.bms.application.exception;

public class PaymentMethodRequiredException extends RuntimeException {
    public PaymentMethodRequiredException() {
        super("A payment method is required before trying to reserve a bike. Please add a payment option to your account.");
    }
}
