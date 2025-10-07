package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.DockingStation;

public interface StationRepository extends JpaRepository<DockingStation, Long> { }
