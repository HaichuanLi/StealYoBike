package com.acme.bms.api.trip;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import com.acme.bms.application.usecase.UC16_SearchRidebyTripID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final UC16_SearchRidebyTripID searchRidebyTripID;

    @GetMapping("/{tripId}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable Long tripId) {
        TripResponse response = searchRidebyTripID.execute(tripId);
        return ResponseEntity.ok(response);
    }
}