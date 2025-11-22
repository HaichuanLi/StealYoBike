package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Status.TripStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Trip findByRiderIdAndStatus(Long riderId, TripStatus status);
    java.util.List<Trip> findAllByRiderIdAndStatus(Long riderId, TripStatus status);
    java.util.List<Trip> findAllByStatus(TripStatus status);
    java.util.List<Trip> findAllByStatusOrderByEndTimeDesc(TripStatus status);
    java.util.List<Trip> findAllByRiderIdAndStatusOrderByEndTimeDesc(Long riderId, TripStatus status);

    @Query("SELECT COUNT(t) FROM Trip t WHERE t.rider.id = :riderId AND t.startTime >= :since")
    int countByUserSince(@Param("riderId") Long riderId,
                         @Param("since") LocalDateTime since);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN TRUE ELSE FALSE END " +
            "FROM Trip t WHERE t.rider.id = :riderId AND t.status = 'STARTED'")
    boolean hasUnreturnedBike(@Param("riderId") Long riderId);

    @Query("SELECT COUNT(t) FROM Trip t " +
            "WHERE t.rider.id = :riderId " +
            "AND t.startTime >= :startTime " +
            "AND t.startTime < :endTime")
    int countTripsPerMonth(@Param("riderId") Long riderId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(t) FROM Trip t " +
            "WHERE t.rider.id = :riderId " +
            "AND t.startTime >= :startTime " +
            "AND t.startTime < :endTime")
    int countTripsPerWeek(@Param("riderId") Long riderId,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}
