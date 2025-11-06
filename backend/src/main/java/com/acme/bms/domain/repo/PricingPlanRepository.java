package com.acme.bms.domain.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.PricingPlan;
import com.acme.bms.domain.entity.PricingPlanType;

public interface PricingPlanRepository extends JpaRepository<PricingPlan, Long> {
    Optional<PricingPlan> findByType(PricingPlanType type);
}
