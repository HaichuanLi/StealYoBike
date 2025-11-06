package com.acme.bms.application.usecase;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import com.acme.bms.api.pricing.PricingResponse;
import com.acme.bms.domain.entity.PricingPlan;
import com.acme.bms.domain.entity.PricingPlanType;
import com.acme.bms.domain.repo.PricingPlanRepository;

@Service
@RequiredArgsConstructor
public class UC9_GetCurrentPricingUseCase {

    private final PricingPlanRepository pricingPlanRepository;

    public PricingResponse execute() {
        PricingPlan standardPlan = pricingPlanRepository.findByType(PricingPlanType.STANDARD).orElse(null);
        PricingPlan eBikePlan = pricingPlanRepository.findByType(PricingPlanType.EBIKE).orElse(null);

        PricingResponse.StandardBikePricing standardPricing = (standardPlan == null)
                ? new PricingResponse.StandardBikePricing(1.50, 0.15)
                : new PricingResponse.StandardBikePricing(
                standardPlan.getBaseFee(),
                standardPlan.getPerMinuteRate()
        );

        PricingResponse.EBikePricing eBikePricing = (eBikePlan == null)
                ? new PricingResponse.EBikePricing(2.50, 0.25, 1.00)
                : new PricingResponse.EBikePricing(
                eBikePlan.getBaseFee(),
                eBikePlan.getPerMinuteRate(),
                eBikePlan.getSurcharge() != null ? eBikePlan.getSurcharge() : 0.0
        );

        System.out.println("UC9_GetCurrentPricingUseCase");
        System.out.printf("Standard Bike -> Base Fee: %.2f | Rate: %.2f%n",
                standardPricing.baseFee(), standardPricing.perMinuteRate());
        System.out.printf("E-Bike -> Base Fee: %.2f | Rate: %.2f | Surcharge: %.2f%n",
                eBikePricing.baseFee(), eBikePricing.perMinuteRate(), eBikePricing.surcharge());

        return new PricingResponse(standardPricing, eBikePricing);
    }
}
