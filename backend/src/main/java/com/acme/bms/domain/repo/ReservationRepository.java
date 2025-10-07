package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> { }
