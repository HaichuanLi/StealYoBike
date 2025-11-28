package com.acme.bms.application.service;

import org.springframework.stereotype.Service;

import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;

    public User updateActiveRole(Long userId, String role) {

        if (!role.equalsIgnoreCase("RIDER") && !role.equalsIgnoreCase("OPERATOR")) {
            throw new IllegalArgumentException("Invalid role");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Operator or Admin can switch to rider
        if (user.getRole() == Role.OPERATOR || user.getRole() == Role.ADMIN) {
            user.setActiveRole(role.toUpperCase());
        } else {
            // Rider can only act as rider
            user.setActiveRole("RIDER");
        }

        return userRepository.save(user);
    }
}
