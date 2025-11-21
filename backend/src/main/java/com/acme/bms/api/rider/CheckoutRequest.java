package com.acme.bms.api.rider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
                @NotNull Long reservationId,
                @NotBlank String pin) {
}
