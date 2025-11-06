package com.acme.bms.application.service;

import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.repo.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryService {

    private final ReservationRepository reservationRepository;

    /**
     * Periodically checks for expired reservations and marks them as EXPIRED.
     * Runs every 30 seconds to ensure timely expiration handling.
     */
    @Scheduled(fixedRate = 30000) // Check every 30 seconds
    @Transactional
    public void expireReservations() {
        Instant now = Instant.now();

        List<Reservation> activeReservations = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.ACTIVE, now);

        for (Reservation reservation : activeReservations) {
            log.info("Expiring reservation {} for bike {} (rider: {})",
                    reservation.getId(),
                    reservation.getBike().getId(),
                    reservation.getRider().getUsername());

            reservation.setStatus(ReservationStatus.EXPIRED);

            // When a reservation expires, the bike was never checked out - it's still in
            // its dock
            // Need to transition it from RESERVED back to AVAILABLE
            var bike = reservation.getBike();
            if (bike.getDock() != null) {
                bike.getState().returnBike(bike.getDock());
            } else {
                log.warn("Bike {} has no dock associated - cannot return to available state", bike.getId());
            }

            reservationRepository.save(reservation);
        }

        if (!activeReservations.isEmpty()) {
            log.info("Expired {} reservations at {}", activeReservations.size(), now);
        }
    }
}
