package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.acme.bms.api.pricing.PricingResponse;
import com.acme.bms.domain.entity.PricingPlan;
import com.acme.bms.domain.entity.PricingPlanType;
import com.acme.bms.domain.repo.PricingPlanRepository;

class UC9Test {

    @Test
    void execute_shouldReturnDatabaseValues_whenPlansExist() {
        System.out.println("TEST: Database contains both STANDARD and E-BIKE pricing");

        PricingPlanRepository mockRepository = mock(PricingPlanRepository.class);
        UC9_GetCurrentPricingUseCase useCase = new UC9_GetCurrentPricingUseCase(mockRepository);

        PricingPlan standardPlan = PricingPlan.builder()
                .id(1L)
                .type(PricingPlanType.STANDARD)
                .baseFee(1.80)
                .perMinuteRate(0.20)
                .build();

        PricingPlan eBikePlan = PricingPlan.builder()
                .id(2L)
                .type(PricingPlanType.EBIKE)
                .baseFee(2.90)
                .perMinuteRate(0.28)
                .surcharge(1.15)
                .build();

        when(mockRepository.findByType(PricingPlanType.STANDARD)).thenReturn(Optional.of(standardPlan));
        when(mockRepository.findByType(PricingPlanType.EBIKE)).thenReturn(Optional.of(eBikePlan));

        System.out.println("Before execution → Database values loaded");
        PricingResponse response = useCase.execute();
        System.out.println("After execution → Response generated");

        assertThat(response.standardBike().baseFee()).isEqualTo(1.80);
        assertThat(response.standardBike().perMinuteRate()).isEqualTo(0.20);
        assertThat(response.eBike().baseFee()).isEqualTo(2.90);
        assertThat(response.eBike().perMinuteRate()).isEqualTo(0.28);
        assertThat(response.eBike().surcharge()).isEqualTo(1.15);
    }

    @Test
    void execute_shouldFallbackToDefaults_whenPlansMissing() {
        System.out.println("TEST: Database is empty (fallback defaults used)");

        PricingPlanRepository mockRepository = mock(PricingPlanRepository.class);
        UC9_GetCurrentPricingUseCase useCase = new UC9_GetCurrentPricingUseCase(mockRepository);

        when(mockRepository.findByType(PricingPlanType.STANDARD)).thenReturn(Optional.empty());
        when(mockRepository.findByType(PricingPlanType.EBIKE)).thenReturn(Optional.empty());

        System.out.println("Before execution → No data in DB");
        PricingResponse response = useCase.execute();
        System.out.println("After execution → Default values returned");

        assertThat(response.standardBike().baseFee()).isEqualTo(1.50);
        assertThat(response.standardBike().perMinuteRate()).isEqualTo(0.15);
        assertThat(response.eBike().baseFee()).isEqualTo(2.50);
        assertThat(response.eBike().perMinuteRate()).isEqualTo(0.25);
        assertThat(response.eBike().surcharge()).isEqualTo(1.00);
    }
}
