package com.acme.bms.application.service;

import com.acme.bms.domain.entity.Tier;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.TripRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class DefaultTierEvaluationService implements TierEvaluationService {

        private final TripRepository tripRepo;
        private final ReservationRepository reservationRepo;

        public DefaultTierEvaluationService(
                        TripRepository tripRepo,
                        ReservationRepository reservationRepo) {
                this.tripRepo = tripRepo;
                this.reservationRepo = reservationRepo;
        }

        @Override
        public Tier evaluate(Long userId) {

                LocalDateTime now = LocalDateTime.now();

                int tripsLastYear = tripRepo.countByUserSince(userId, now.minusYears(1));

                int tripsLast3Months = tripRepo.countTripsPerMonth(
                                userId,
                                now.minusMonths(3),
                                now);

                int tripsLast12Weeks = tripRepo.countTripsPerWeek(
                                userId,
                                now.minusWeeks(12),
                                now);

                boolean hasUnreturned = tripRepo.hasUnreturnedBike(userId);

                Instant since1y = now.minusYears(1)
                                .atZone(ZoneId.systemDefault())
                                .toInstant();

                int missedReservation = reservationRepo.countMissedByUserSince(userId, since1y);

                int successfulReservation = reservationRepo.countSuccessfulByUserSince(userId, since1y);

                boolean bronze = missedReservation == 0
                                && !hasUnreturned
                                && tripsLastYear > 10;

                if (!bronze) {
                        return Tier.REGULAR;
                }

                boolean silver = successfulReservation >= 5
                                && tripsLast3Months >= 15; // 5 * 3 months

                if (!silver) {
                        return Tier.BRONZE;
                }

                boolean gold = tripsLast12Weeks >= 60; // 5 * 12 weeks

                if (!gold) {
                        return Tier.SILVER;
                }

                return Tier.GOLD;
        }
}
