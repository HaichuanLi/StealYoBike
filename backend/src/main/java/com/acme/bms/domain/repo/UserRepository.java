package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acme.bms.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
