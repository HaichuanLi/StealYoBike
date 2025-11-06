package com.acme.bms.api.rider;

public record ReservationCancelResponse(
        Long reservationId,
        String status) {
}
