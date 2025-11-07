package com.acme.bms.api.auth;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.bms.application.usecase.UC1_RegisterUserUseCase;
import com.acme.bms.application.usecase.UC2_LoginUserUseCase;
import com.acme.bms.application.usecase.UC10_ProvidePaymentMethod;
import com.acme.bms.application.exception.UserNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.acme.bms.domain.repo.UserRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UC1_RegisterUserUseCase registerUC;
    private final UC2_LoginUserUseCase loginUC;
    private final UC10_ProvidePaymentMethod providePaymentUC;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var out = registerUC.execute(request);
        return ResponseEntity.created(URI.create("/api/users/" + out.id())).body(out); // 201 + Location
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUC.execute(request));
    }

    @PutMapping("/me/payment-token")
    public ResponseEntity<UserInfoResponse> updatePaymentToken(
        @AuthenticationPrincipal String userId,
        @Valid @RequestBody UpdatePaymentTokenRequest request) {
        try {
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

            log.info("Updating payment token for user: {}", userIdLong);

            UserInfoResponse response = providePaymentUC.execute(request, userIdLong);

            log.info("Updated payment token for user: {}", response.username());

            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error("Error updating payment token", e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            log.warn("No authentication principal found for /me request");
            return ResponseEntity.status(401).build();
        }

        String principalName = authentication.getName();

        try {
            Long userId = Long.parseLong(principalName);
            return ResponseEntity.of(
                    userRepository.findById(userId)
                            .map(dbUser -> new UserInfoResponse(
                                    dbUser.getId(),
                                    dbUser.getEmail(),
                                    dbUser.getUsername(),
                                    dbUser.getFullName(),
                                    dbUser.getRole().name(),
                                    dbUser.getPaymentToken())));
        } catch (NumberFormatException ex) {
            return ResponseEntity.of(
                    userRepository.findByUsername(principalName)
                            .map(dbUser -> new UserInfoResponse(
                                    dbUser.getId(),
                                    dbUser.getEmail(),
                                    dbUser.getUsername(),
                                    dbUser.getFullName(),
                                    dbUser.getRole().name(),
                                    dbUser.getPaymentToken())));
        }
    }
}
