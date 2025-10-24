package com.acme.bms.api.rider;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.acme.bms.application.usecase.UC3_ReserveCheckoutUseCase;
import com.acme.bms.application.usecase.UC4_ReturnBikeUseCase;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderController {

    private final UC3_ReserveCheckoutUseCase reserveUC;
    private final UC4_ReturnBikeUseCase returnUC;

    // UC3: Reserve a bike
    @PostMapping("/reserve")
    public ResponseEntity<ReserveBikeResponse> reserveBike(@Valid @RequestBody ReserveBikeRequest request) {
        return ResponseEntity.ok(reserveUC.execute(request));
    }

    // UC4: Return a bike
    @PostMapping("/return")
    public ResponseEntity<ReturnBikeResponse> returnBike(@Valid @RequestBody ReturnBikeRequest request) {
        return ResponseEntity.ok(returnUC.execute(request));
    }
}
