package com.acme.bms.api.rider;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.acme.bms.application.usecase.UC3_ReserveCheckoutUseCase;
import com.acme.bms.application.usecase.UC4_ReturnBikeUseCase;
import com.acme.bms.application.usecase.UC11_RiderCheckTripBill;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderController {

    private final UC3_ReserveCheckoutUseCase reserveUC;
    private final UC4_ReturnBikeUseCase returnUC;
    private final UC11_RiderCheckTripBill billUC;
    private final com.acme.bms.application.usecase.UC12_PayTripBill uc12;

    // UC3: Reserve a bike
    @PostMapping("/reserve")
    public ResponseEntity<ReserveBikeResponse> reserveBike(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody ReserveBikeRequest request) {
        Long riderId = parsePrincipalToLong(principal);
        // pass current username to the use case
        ReserveBikeResponse out = reserveUC.execute(request, riderId);
        return ResponseEntity.created(URI.create("/api/rider/reservations/" + out.reservationId())).body(out);
    }

    // UC4: Return a bike
    @PostMapping("/return")
    public ResponseEntity<ReturnBikeResponse> returnBike(@Valid @RequestBody ReturnBikeRequest request) {
        return ResponseEntity.ok(returnUC.execute(request));
    }

    @GetMapping("/trips/{tripId}/bill")
    public ResponseEntity<com.acme.bms.api.rider.TripBillResponse> getTripBill(
            @AuthenticationPrincipal String principal,
            @PathVariable Long tripId) {
        Long riderId = parsePrincipalToLong(principal);
        var resp = billUC.execute(tripId, riderId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/trips/{tripId}/pay")
    public ResponseEntity<com.acme.bms.api.rider.TripBillResponse> payTripBill(
            @PathVariable Long tripId,
            @RequestBody com.acme.bms.api.rider.PayBillRequest req,
            @AuthenticationPrincipal String principal) {
        Long riderId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(uc12.execute(tripId, req.paymentToken(), riderId));
    }

    // quickly verify authentication and display the correct user state
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return Map.of("username", user != null ? user.getUsername() : null);
    }

    @GetMapping("/current-reservation")
    public ResponseEntity<ReservationInfoResponse> getCurrentReservation(
            @AuthenticationPrincipal String principal) {
        Long riderId = parsePrincipalToLong(principal);
        ReservationInfoResponse res = reserveUC.getCurrentReservation(riderId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/cancel-reservation")
    public ResponseEntity<ReservationCancelResponse> cancelReservation(
            @AuthenticationPrincipal String principal) {
        Long riderId = parsePrincipalToLong(principal);
        ReservationCancelResponse res = reserveUC.cancelCurrentReservation(riderId);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkoutBike(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody CheckoutRequest request) {
        Long riderId = parsePrincipalToLong(principal);
        CheckoutResponse res = reserveUC.checkoutBike(request, riderId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/current-trip")
    public ResponseEntity<TripInfoResponse> getCurrentTrip(
            @AuthenticationPrincipal String principal) {
        Long riderId = parsePrincipalToLong(principal);
        TripInfoResponse res = reserveUC.getCurrentTrip(riderId);
        if (res == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(res);
    }

    private Long parsePrincipalToLong(Object principal) {
        if (principal == null) {
            throw new IllegalArgumentException("No authenticated principal available");
        }
        if (principal instanceof String) {
            return Long.valueOf((String) principal);
        } else if (principal instanceof Long) {
            return (Long) principal;
        }
        return Long.valueOf(principal.toString());
    }
}
