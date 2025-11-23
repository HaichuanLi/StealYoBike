package com.acme.bms.domain.repo;

import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant expirationTime);

    Reservation findByRiderIdAndStatus(Long riderId, ReservationStatus status);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.rider.id = :userId AND r.status = 'FULFILLED' AND r.createdAt >= :since")
    int countSuccessfulByUserSince(@Param("userId") Long userId, @Param("since") Instant since);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.rider.id = :userId AND r.status = 'EXPIRED' AND r.createdAt >= :since")
    int countMissedByUserSince(@Param("userId") Long userId, @Param("since") Instant since);
}
