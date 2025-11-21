package com.acme.bms.domain.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.acme.bms.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username); // ← needed by UC3

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email); // ← used by login

    default Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        Optional<User> user = findByUsername(usernameOrEmail);
        if (user.isPresent()) {
            return user;
        }
        return findByEmail(usernameOrEmail);
    }
}
