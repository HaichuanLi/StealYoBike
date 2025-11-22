package com.acme.bms.application.service;

import com.acme.bms.domain.entity.Tier;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.TripRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

class DefaultTierEvaluationServiceTest {

    private TripRepository tripRepo;
    private ReservationRepository reservationRepo;
    private DefaultTierEvaluationService service;

    @BeforeEach
    void setUp() {
        tripRepo = mock(TripRepository.class);
        reservationRepo = mock(ReservationRepository.class);
        service = new DefaultTierEvaluationService(tripRepo, reservationRepo);
    }

    @Test
    void evaluate_regular_whenBronzeConditionsFail() {
        System.out.println("\n=== TierEvaluationServiceTest: REGULAR (Bronze conditions fail) ===");
        Long userId = 1L;

        System.out.println("[Before] Setting mocked values:");
        System.out.println("    - tripsLastYear = 5 (< 10 required)");
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(5);

        System.out.println("    - missedReservation = 0");
        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(0);

        System.out.println("    - hasUnreturned = false");
        when(tripRepo.hasUnreturnedBike(userId))
                .thenReturn(false);

        System.out.println("    - successfulReservation = 0");
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(0);

        System.out.println("    - tripsPerMonth = 0");
        when(tripRepo.countTripsPerMonth(eq(userId), any(), any()))
                .thenReturn(0);

        System.out.println("    - tripsPerWeek = 0");
        when(tripRepo.countTripsPerWeek(eq(userId), any(), any()))
                .thenReturn(0);

        System.out.println("\n[Action] Evaluating tier...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Checking result...");
        assertThat(tier).isEqualTo(Tier.REGULAR);
        System.out.println("[Success] Tier = REGULAR because Bronze was not satisfied.\n");
    }

    @Test
    void evaluate_bronze_whenOnlyBronzeConditionsMet() {
        System.out.println("\n=== TierEvaluationServiceTest: BRONZE ===");
        Long userId = 2L;

        System.out.println("[Before] Setting bronze-valid mocks:");
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(11);
        System.out.println("    - tripsLastYear = 11 (OK)");

        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(0);
        System.out.println("    - missedReservation = 0 (OK)");

        when(tripRepo.hasUnreturnedBike(userId))
                .thenReturn(false);
        System.out.println("    - hasUnreturned = false (OK)");

        System.out.println("[Silver] but failing Silver conditions...");
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class)))
                .thenReturn(2);
        System.out.println("    - successfulReservation = 2 (<5)");

        when(tripRepo.countTripsPerMonth(eq(userId), any(), any()))
                .thenReturn(1);
        System.out.println("    - tripsPerMonth = 1 (<5)");

        when(tripRepo.countTripsPerWeek(eq(userId), any(), any()))
                .thenReturn(1);

        System.out.println("\n[Action] Evaluating...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Tier should be BRONZE.");
        assertThat(tier).isEqualTo(Tier.BRONZE);
        System.out.println("[Success] Bronze logic validated.\n");
    }

    @Test
    void evaluate_silver_whenSilverConditionsMet() {
        System.out.println("\n=== TierEvaluationServiceTest: SILVER ===");
        Long userId = 3L;

        System.out.println("[Before] Setting bronze-valid data:");
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(20);
        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(0);
        when(tripRepo.hasUnreturnedBike(userId)).thenReturn(false);

        System.out.println("[Silver] meeting Silver conditions:");
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(6);
        System.out.println("    - successfulReservation = 6 (>=5)");
        when(tripRepo.countTripsPerMonth(eq(userId), any(), any())).thenReturn(5);
        System.out.println("    - tripsPerMonth = 5 (>=5)");

        System.out.println("[Gold] failing gold conditions");
        when(tripRepo.countTripsPerWeek(eq(userId), any(), any())).thenReturn(3);

        System.out.println("\n[Action] Evaluating tier...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Expecting Tier = SILVER");
        assertThat(tier).isEqualTo(Tier.SILVER);
        System.out.println("[Success] Silver logic validated.\n");
    }

    @Test
    void evaluate_gold_whenAllConditionsMet() {
        System.out.println("\n=== TierEvaluationServiceTest: GOLD ===");
        Long userId = 4L;

        System.out.println("[Before] Bronze conditions OK");
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(15);
        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(0);
        when(tripRepo.hasUnreturnedBike(userId)).thenReturn(false);

        System.out.println("[Silver] OK");
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(7);
        when(tripRepo.countTripsPerMonth(eq(userId), any(), any())).thenReturn(6);

        System.out.println("[Gold] OK");
        when(tripRepo.countTripsPerWeek(eq(userId), any(), any())).thenReturn(5);

        System.out.println("\n[Action] Evaluating tier...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Expecting GOLD");
        assertThat(tier).isEqualTo(Tier.GOLD);
        System.out.println("[Success] Gold logic validated.\n");
    }
    @Test
    void downgrade_goldToSilver_whenGoldConditionFails() {
        System.out.println("\n=== TierEvaluationServiceTest: GOLD → SILVER downgrade ===");

        Long userId = 50L;

        // Bronze OK
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(20);
        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(0);
        when(tripRepo.hasUnreturnedBike(userId)).thenReturn(false);

        // Silver OK
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(6);
        when(tripRepo.countTripsPerMonth(eq(userId), any(), any())).thenReturn(5);

        // Gold FAIL
        when(tripRepo.countTripsPerWeek(eq(userId), any(), any())).thenReturn(3);

        System.out.println("[Before] Gold condition fails:");
        System.out.println("    tripsPerWeek = 3 (<5 required for GOLD)");

        System.out.println("\n[Action] Evaluating tier...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Expected SILVER due to gold rule failure.");
        assertThat(tier).isEqualTo(Tier.SILVER);

        System.out.println("[Success] GOLD → SILVER downgrade validated.\n");
    }

    @Test
    void downgrade_silverToBronze_whenSilverConditionsFail() {
        System.out.println("\n=== TierEvaluationServiceTest: SILVER → BRONZE downgrade ===");

        Long userId = 60L;

        // Bronze OK
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(20);
        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(0);
        when(tripRepo.hasUnreturnedBike(userId)).thenReturn(false);

        // Silver FAIL
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(3); // <5
        when(tripRepo.countTripsPerMonth(eq(userId), any(), any())).thenReturn(2); // <5

        // Gold irrelevant
        when(tripRepo.countTripsPerWeek(eq(userId), any(), any())).thenReturn(10);

        System.out.println("[Before] Silver conditions fail:");
        System.out.println("    successfulReservation = 3 (<5)");
        System.out.println("    tripsPerMonth = 2 (<5)");

        System.out.println("\n[Action] Evaluating tier...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Expected BRONZE.");
        assertThat(tier).isEqualTo(Tier.BRONZE);

        System.out.println("[Success] SILVER → BRONZE downgrade validated.\n");
    }


    @Test
    void downgrade_bronzeToRegular_whenBronzeConditionsFail() {
        System.out.println("\n=== TierEvaluationServiceTest: BRONZE → REGULAR downgrade ===");

        Long userId = 70L;

        // Bronze FAIL — tripsLastYear < 10
        when(tripRepo.countByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(8);

        // Other bronze checks irrelevant
        when(reservationRepo.countMissedByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(0);
        when(tripRepo.hasUnreturnedBike(userId)).thenReturn(false);

        // Silver irrelevant
        when(reservationRepo.countSuccessfulByUserSince(eq(userId), any(LocalDateTime.class))).thenReturn(10);
        when(tripRepo.countTripsPerMonth(eq(userId), any(), any())).thenReturn(10);
        when(tripRepo.countTripsPerWeek(eq(userId), any(), any())).thenReturn(10);

        System.out.println("[Before] Bronze condition fails:");
        System.out.println("    tripsLastYear = 8 (<10 required)");

        System.out.println("\n[Action] Evaluating tier...");
        Tier tier = service.evaluate(userId);

        System.out.println("[After] Expected REGULAR.");
        assertThat(tier).isEqualTo(Tier.REGULAR);

        System.out.println("[Success] BRONZE → REGULAR downgrade validated.\n");
    }
}
