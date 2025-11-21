package com.acme.bms.domain.repo;

import com.acme.bms.domain.entity.DockingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DockingStationRepository extends JpaRepository<DockingStation, Long> {
}
