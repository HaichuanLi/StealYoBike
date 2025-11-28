package com.acme.bms.api.rider;

import com.acme.bms.application.usecase.*;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.DockingStationRepository;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.domain.repo.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderController {

    private final UC3_ReserveCheckoutUseCase reserveUC;
    private final UC4_ReturnBikeUseCase returnUC;
    private final UC11_RiderCheckTripBill billUC;
    private final UC13_ListPastTrips listPastTripsUC;
    private final com.acme.bms.application.usecase.UC12_PayTripBill uc12;
    private final UC15_GetTripDetails uc15;
    private final UserRepository userRepository;
    private final DockingStationRepository stationRepository;
    private final TripRepository tripRepository;

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
    public ResponseEntity<ReturnBikeResponse> returnBike(@Valid @RequestBody ReturnBikeRequest request,
            @AuthenticationPrincipal String principal) {
        Long riderId = Long.parseLong(principal);
        return ResponseEntity.ok(returnUC.execute(request, riderId));
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

    @GetMapping("/trips/history")
    public ResponseEntity<java.util.List<com.acme.bms.api.rider.PastTripResponse>> getPastTrips(
            @AuthenticationPrincipal String principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(required = false, name = "type") String bikeType) {
        Long riderId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(listPastTripsUC.execute(riderId, fromDate, toDate, bikeType));
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

    @PutMapping("/me/plan")
    @Transactional
    public ResponseEntity<com.acme.bms.api.auth.UserInfoResponse> updatePlan(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody UpdatePlanRequest request) {

        Long riderId = parsePrincipalToLong(principal);

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        rider.setPlan(request.plan());
        userRepository.save(rider);

        // Return updated user info
        return ResponseEntity.ok(toUserInfo(rider));
    }

    @GetMapping("/trips/{tripId}/details")
    public ResponseEntity<com.acme.bms.api.trip.TripResponse> getTripDetailsForRider(
            @AuthenticationPrincipal String principal,
            @PathVariable Long tripId) {
        Long riderId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(uc15.forRider(riderId, tripId));
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

    @PostMapping("/stations/{stationId}/subscribe")
    @Transactional
    public ResponseEntity<SubscriptionResponse> subscribeToStation(
            @AuthenticationPrincipal String principal,
            @PathVariable Long stationId) {

        Long riderId = Long.parseLong(principal);

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DockingStation station = stationRepository.findByIdWithObservers(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        // Check if already subscribed
        if (station.getObservers().contains(rider)) {
            throw new RuntimeException("Already subscribed to this station");
        }

        station.addObserver(rider);
        stationRepository.save(station);

        return ResponseEntity.ok(new SubscriptionResponse(
                station.getId(),
                station.getName(),
                true,
                "Subscribed successfully"));
    }

    @DeleteMapping("/stations/{stationId}/unsubscribe")
    @Transactional
    public ResponseEntity<SubscriptionResponse> unsubscribeFromStation(
            @AuthenticationPrincipal String principal,
            @PathVariable Long stationId) {

        Long riderId = Long.parseLong(principal);

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DockingStation station = stationRepository.findByIdWithObservers(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        station.removeObserver(rider);
        stationRepository.save(station);

        return ResponseEntity.ok(new SubscriptionResponse(
                station.getId(),
                station.getName(),
                false,
                "Unsubscribed successfully"));
    }

    @GetMapping("/subscriptions")
    @Transactional(readOnly = true)
    public ResponseEntity<List<StationSubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal String principal) {

        Long riderId = Long.parseLong(principal);

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find all stations where this user is an observer
        List<DockingStation> subscribedStations = stationRepository.findAllWithObservers().stream()
                .filter(station -> station.getObservers().contains(rider))
                .collect(Collectors.toList());

        List<StationSubscriptionResponse> subscriptions = subscribedStations.stream()
                .map(station -> new StationSubscriptionResponse(
                        station.getId(),
                        station.getName(),
                        station.getStreetAddress(),
                        station.getAllAvailableBikes(),
                        station.getCapacity(),
                        station.getAvailabilityPercentage()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/stations/{stationId}/is-subscribed")
    @Transactional(readOnly = true)
    public ResponseEntity<Boolean> isSubscribed(
            @AuthenticationPrincipal String principal,
            @PathVariable Long stationId) {

        Long riderId = Long.parseLong(principal);

        User rider = userRepository.findById(riderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DockingStation station = stationRepository.findByIdWithObservers(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        boolean isSubscribed = station.getObservers().contains(rider);

        return ResponseEntity.ok(isSubscribed);
    }

    private com.acme.bms.api.auth.UserInfoResponse toUserInfo(User user) {
        boolean dualRole = (user.getRole() == Role.OPERATOR || user.getRole() == Role.ADMIN);

        String activeRole = (user.getActiveRole() != null)
                ? user.getActiveRole()
                : user.getRole().name();

        int tripsLastYear = tripRepository.countByUserSince(
                user.getId(),
                LocalDateTime.now().minusYears(1));

        int tripsLast3Months = tripRepository.countTripsPerMonth(
                user.getId(),
                LocalDateTime.now().minusMonths(3),
                LocalDateTime.now());

        int tripsLast12Weeks = tripRepository.countTripsPerWeek(
                user.getId(),
                LocalDateTime.now().minusWeeks(12),
                LocalDateTime.now());

        return new com.acme.bms.api.auth.UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                activeRole,
                dualRole,
                user.getPaymentToken(),
                user.getPlan() != null ? user.getPlan().name() : null,
                user.getTier() != null ? user.getTier().name() : "REGULAR",
                user.getFlexDollar(),
                tripsLastYear,
                tripsLast3Months,
                tripsLast12Weeks);
    }
}
