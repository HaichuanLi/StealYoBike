package com.acme.bms.api.pricing;

public record PricingResponse(
        StandardBikePricing standardBike,
        EBikePricing eBike) {
    public record StandardBikePricing(double baseFee, double perMinuteRate) {
    }

    public record EBikePricing(double baseFee, double perMinuteRate, double surcharge) {
    }
}
