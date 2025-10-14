package com.acme.bms.domain.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.acme.bms.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsernameOrEmail(String id, String email);
}
