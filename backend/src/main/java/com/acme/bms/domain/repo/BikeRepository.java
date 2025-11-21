package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.acme.bms.domain.entity.Bike;

public interface BikeRepository extends JpaRepository<Bike, Long> {
}
