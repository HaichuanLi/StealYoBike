package com.acme.bms.application.usecase;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.acme.bms.api.auth.RegisterRequest;
import com.acme.bms.api.auth.RegisterResponse;
import com.acme.bms.application.events.UserRegisteredEvent;
import com.acme.bms.application.exception.EmailAlreadyUsedException;
import com.acme.bms.application.exception.UsernameAlreadyUsedException;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UC1_RegisterUserUseCase {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;

    @Transactional
    public RegisterResponse execute(RegisterRequest req) {
        if (users.existsByEmail(req.email())) throw new EmailAlreadyUsedException();
        if (users.existsByUsername(req.username())) throw new UsernameAlreadyUsedException();

        User saved = users.save(User.builder()
                .fullName(req.fullName())
                .address(req.address())
                .email(req.email())
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .paymentToken(req.paymentToken())
                .role(Role.RIDER)
                .build());

        events.publishEvent(new UserRegisteredEvent(saved.getId(), saved.getRole().name(), saved.getEmail()));

        return new RegisterResponse(saved.getId(), saved.getEmail(), saved.getUsername(), saved.getRole().name());
    }
}
