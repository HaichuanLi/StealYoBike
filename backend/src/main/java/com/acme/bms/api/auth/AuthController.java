package com.acme.bms.api.auth;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.acme.bms.application.events.TierChangedEvent;
import com.acme.bms.application.service.TierEvaluationService;
import com.acme.bms.application.usecase.UC1_RegisterUserUseCase;
import com.acme.bms.application.usecase.UC2_LoginUserUseCase;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Tier;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.domain.repo.TripRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final UC1_RegisterUserUseCase registerUC;
    private final UC2_LoginUserUseCase loginUC;
    private final TierEvaluationService tierEvaluationService;
    private final ApplicationEventPublisher eventPublisher;
    private final TripRepository tripRepository;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var out = registerUC.execute(request);
        return ResponseEntity.created(URI.create("/api/users/" + out.id())).body(out);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUC.execute(request));
    }

    @PutMapping("/me/payment-token")
    public ResponseEntity<UserInfoResponse> updatePaymentToken(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody UpdatePaymentTokenRequest request) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        Optional<User> userOpt = resolveUser(principal);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOpt.get();
        user.setPaymentToken(request.paymentToken());
        userRepository.save(user);

        return ResponseEntity.ok(toUserInfo(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }

        String principalName = authentication.getName();
        Optional<User> userOpt;

        try {
            Long userId = Long.parseLong(principalName);
            userOpt = userRepository.findById(userId);
        } catch (NumberFormatException ex) {
            userOpt = userRepository.findByUsernameOrEmail(principalName, principalName);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        User user = userOpt.get();

        // Auto-update tier
        Tier evaluatedTier = tierEvaluationService.evaluate(user.getId());
        if (evaluatedTier != user.getTier()) {
            Tier previousTier = user.getTier();
            user.setTier(evaluatedTier);
            userRepository.save(user);

            eventPublisher.publishEvent(new TierChangedEvent(
                    user.getId(),
                    previousTier,
                    evaluatedTier));
        }

        return ResponseEntity.ok(toUserInfo(user));
    }

    @PutMapping("/active-role")
    public ResponseEntity<UserInfoResponse> updateActiveRole(
            Authentication auth,
            @RequestBody ActiveRoleRequest request) {
        if (auth == null)
            return ResponseEntity.status(401).build();

        Optional<User> userOpt = resolveUser(auth.getName());
        if (userOpt.isEmpty())
            return ResponseEntity.status(404).build();

        User user = userOpt.get();

        // Only operators can switch views
        if (user.getRole() != Role.OPERATOR) {
            return ResponseEntity.status(403).build();
        }

        String newRole = request.role().toUpperCase();
        if (!newRole.equals("RIDER") && !newRole.equals("OPERATOR")) {
            return ResponseEntity.badRequest().build();
        }

        user.setActiveRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok(toUserInfo(user));
    }

    private Optional<User> resolveUser(String principal) {
        try {
            Long userId = Long.parseLong(principal);
            return userRepository.findById(userId);
        } catch (NumberFormatException ex) {
            return userRepository.findByUsernameOrEmail(principal, principal);
        }
    }

    private UserInfoResponse toUserInfo(User user) {

        boolean dualRole = (user.getRole() == Role.OPERATOR || user.getRole() == Role.ADMIN);

        String activeRole = (user.getActiveRole() != null)
                ? user.getActiveRole()
                : user.getRole().name();

        int tripsLastYear = tripRepository.countByUserSince(
                user.getId(),
                LocalDateTime.now().minusYears(1));

        int tripsLast3Months = tripRepository.countTripsPerMonth(
                user.getId(),
                LocalDateTime.now().minusMonths(3),
                LocalDateTime.now());

        int tripsLast12Weeks = tripRepository.countTripsPerWeek(
                user.getId(),
                LocalDateTime.now().minusWeeks(12),
                LocalDateTime.now());

        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().name(),
                activeRole,
                dualRole,
                user.getPaymentToken(),
                user.getPlan() != null ? user.getPlan().name() : null,
                user.getTier() != null ? user.getTier().name() : "REGULAR",
                user.getFlexDollar(),
                tripsLastYear,
                tripsLast3Months,
                tripsLast12Weeks);
    }
}
