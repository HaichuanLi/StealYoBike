package com.acme.bms.application.service;

import com.acme.bms.domain.entity.Tier;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.TripRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DefaultTierEvaluationService implements TierEvaluationService {

    private final TripRepository tripRepo;
    private final ReservationRepository reservationRepo;

    public DefaultTierEvaluationService(
            TripRepository tripRepo,
            ReservationRepository reservationRepo
    ) {
        this.tripRepo = tripRepo;
        this.reservationRepo = reservationRepo;
    }

    @Override
    public Tier evaluate(Long userId) {

        LocalDateTime now = LocalDateTime.now();
        int tripsLastYear = tripRepo.countByUserSince(userId, now.minusYears(1));

        int missedReservation = reservationRepo.countMissedByUserSince(userId, now.minusYears(1));
        int successfulReservation = reservationRepo.countSuccessfulByUserSince(userId, now.minusYears(1));

        boolean hasUnreturned = tripRepo.hasUnreturnedBike(userId);

        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        int tripsPerMonth3m = tripRepo.countTripsPerMonth(userId, threeMonthsAgo, now);

        LocalDateTime twelveWeeksAgo = now.minusWeeks(12);
        int tripsPerWeek12w = tripRepo.countTripsPerWeek(userId, twelveWeeksAgo, now);

        //bronze
        boolean bronze =
                missedReservation == 0 &&
                        !hasUnreturned &&
                        tripsLastYear > 10;

        if (!bronze)
            return Tier.REGULAR;
        //silver
        boolean silver =
                successfulReservation >= 5 &&
                        tripsPerMonth3m >= 5;
        if (!silver)
            return Tier.BRONZE;

        //gold
        boolean gold =
                tripsPerWeek12w >= 5;
        if (!gold)
            return Tier.SILVER;

        return Tier.GOLD;
    }
}
