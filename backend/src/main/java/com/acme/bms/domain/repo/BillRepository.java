package com.acme.bms.domain.repo;

import com.acme.bms.domain.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<Bill, Long> {
	java.util.Optional<Bill> findByTripId(Long tripId);
}
