package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.acme.bms.api.auth.LoginRequest;
import com.acme.bms.api.auth.LoginResponse;
import com.acme.bms.application.events.UserLoggedInEvent;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.infrastructure.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC2LoginUserUseCase {
    
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final JwtTokenProvider jwtToken;

    public LoginResponse execute(LoginRequest request) {
        User user = users.findByUsernameOrEmail(request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or email"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtToken.generateToken(user.getId(), user.getRole().name());

        events.publishEvent(new UserLoggedInEvent(user.getId(), user.getRole().name(), user.getEmail()));

        return new LoginResponse(
                token,
                user.getId(),
                user.getRole().name()
        );
    }
}
