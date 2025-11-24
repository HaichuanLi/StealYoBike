package com.acme.bms.domain.repo;

import com.acme.bms.domain.entity.DockingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DockingStationRepository extends JpaRepository<DockingStation, Long> {
    
    @Query("SELECT DISTINCT s FROM DockingStation s LEFT JOIN FETCH s.observers WHERE s.id = :id")
    Optional<DockingStation> findByIdWithObservers(Long id);
    
    @Query("SELECT DISTINCT s FROM DockingStation s LEFT JOIN FETCH s.observers")
    java.util.List<DockingStation> findAllWithObservers();
}
