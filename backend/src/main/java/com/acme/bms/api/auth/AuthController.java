package com.acme.bms.api.auth;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.bms.application.usecase.UC1_RegisterUserUseCase;
import com.acme.bms.application.usecase.UC2_LoginUserUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.acme.bms.domain.entity.User; // Ensure this is the correct package for the User class
import com.acme.bms.domain.repo.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UC1_RegisterUserUseCase registerUC;
    private final UC2_LoginUserUseCase loginUC;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var out = registerUC.execute(request);
        return ResponseEntity.created(URI.create("/api/users/" + out.id())).body(out); // 201 + Location
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUC.execute(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(@AuthenticationPrincipal String userId) {
        try {
            log.info("Getting user info for userId: {}", userId);

            if (userId == null) {
                log.warn("No authenticated user found");
                return ResponseEntity.status(401).build();
            }

            Long userIdLong;
            try {
                userIdLong = Long.parseLong(userId);
            } catch (NumberFormatException e) {
                log.error("Invalid user ID format: {}", userId);
                return ResponseEntity.status(400).build();
            }

            log.info("Looking up user with ID: {}", userIdLong);

            Optional<User> userOpt = userRepository.findById(userIdLong);

            if (userOpt.isEmpty()) {
                log.error("User not found with ID: {}", userIdLong);
                return ResponseEntity.status(404).build();
            }

            User user = userOpt.get();
            log.info("Found user: {} with email: {}", user.getUsername(), user.getEmail());

            UserInfoResponse response = new UserInfoResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting user info", e);
            return ResponseEntity.status(500).build();
        }
    }
}
