package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.Station;

public interface StationRepository extends JpaRepository<Station, Long> { }
