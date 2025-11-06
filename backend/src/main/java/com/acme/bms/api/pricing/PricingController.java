package com.acme.bms.api.pricing;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.bms.application.usecase.UC9_GetCurrentPricingUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final UC9_GetCurrentPricingUseCase getPricingUC;

    @GetMapping
    public ResponseEntity<PricingResponse> getCurrentPricing() {
        return ResponseEntity.ok(getPricingUC.execute());
    }
}
