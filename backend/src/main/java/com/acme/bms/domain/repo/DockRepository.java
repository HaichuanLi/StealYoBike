package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.Dock;

public interface DockRepository extends JpaRepository<Dock, Long> {
}
