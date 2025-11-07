package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Status.TripStatus;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Trip findByRiderIdAndStatus(Long riderId, TripStatus status);
    java.util.List<Trip> findAllByRiderIdAndStatus(Long riderId, TripStatus status);
}
