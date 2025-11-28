package com.acme.bms.api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.acme.bms.application.usecase.UC1_RegisterUserUseCase;
import com.acme.bms.application.usecase.UC2_LoginUserUseCase;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

        @Mock
        UserRepository userRepository;

        @Mock
        UC1_RegisterUserUseCase registerUC;

        @Mock
        UC2_LoginUserUseCase loginUC;

        @Mock
        com.acme.bms.application.service.TierEvaluationService tierEvaluationService;

        @Mock
        org.springframework.context.ApplicationEventPublisher eventPublisher;

        @Mock
        com.acme.bms.domain.repo.TripRepository tripRepository;

        @InjectMocks
        AuthController controller;

        @Test
        void me_returns_userinfo_when_found() {
                String username = "jdoe";

                com.acme.bms.domain.entity.User dbUser = com.acme.bms.domain.entity.User.builder()
                                .id(1L)
                                .username(username)
                                .email("jdoe@example.com")
                                .fullName("John Doe")
                                .address("addr")
                                .passwordHash("hash")
                                .role(Role.RIDER)
                                .build();

                when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.of(dbUser));
                when(tierEvaluationService.evaluate(1L)).thenReturn(
                                Role.RIDER.equals(dbUser.getRole()) ? com.acme.bms.domain.entity.Tier.REGULAR
                                                : com.acme.bms.domain.entity.Tier.REGULAR);
                when(tripRepository.countByUserSince(eq(1L), any())).thenReturn(0);
                when(tripRepository.countTripsPerMonth(eq(1L), any(), any())).thenReturn(0);
                when(tripRepository.countTripsPerWeek(eq(1L), any(), any())).thenReturn(0);

                org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                                username, "pw", Collections.emptyList());

                org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                principal, null, Collections.emptyList());

                ResponseEntity<UserInfoResponse> resp = controller.me(auth);

                assertEquals(200, resp.getStatusCode().value());
                UserInfoResponse body = resp.getBody();
                assertNotNull(body);
                assertEquals(dbUser.getId(), body.id());
                assertEquals(dbUser.getEmail(), body.email());
                assertEquals(dbUser.getUsername(), body.username());
                assertEquals(dbUser.getFullName(), body.fullName());
                assertEquals(dbUser.getRole().name(), body.role());
        }

        @Test
        void me_returns_404_when_not_found() {
                String username = "missing";
                when(userRepository.findByUsernameOrEmail(username, username)).thenReturn(Optional.empty());

                org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                                username, "pw", Collections.emptyList());

                org.springframework.security.core.Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                principal, null, Collections.emptyList());

                ResponseEntity<UserInfoResponse> resp = controller.me(auth);

                assertEquals(404, resp.getStatusCode().value());
                assertNull(resp.getBody());
        }
}
