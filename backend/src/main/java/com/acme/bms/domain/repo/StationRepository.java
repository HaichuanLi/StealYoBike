package com.acme.bms.domain.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.acme.bms.api.station.StationSummaryDto;
import com.acme.bms.domain.entity.DockingStation;

public interface StationRepository extends JpaRepository<DockingStation, Long> {

    @Query("SELECT new com.acme.bms.api.station.StationSummaryDto(" +
            "s.id, s.name, s.status, s.latitude, s.longitude, s.streetAddress, " +
            "SUM(CASE WHEN d.bike IS NOT NULL THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN d.bike IS NULL THEN 1 ELSE 0 END), s.capacity) " +
            "FROM DockingStation s LEFT JOIN s.docks d " +
            "GROUP BY s.id, s.name, s.status, s.latitude, s.longitude, s.streetAddress, s.capacity")
    List<StationSummaryDto> findAllStationSummaries();

}
