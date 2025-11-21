package com.acme.bms.api.auth;

import javax.validation.constraints.NotBlank;

public record UpdatePaymentTokenRequest(
        @NotBlank(message = "Payment token is required") String paymentToken) {
}
