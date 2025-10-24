package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.acme.bms.api.auth.LoginRequest;
import com.acme.bms.api.auth.LoginResponse;
import com.acme.bms.application.events.UserLoggedInEvent;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.infrastructure.security.JwtTokenProvider;

class UC2Test {

    @Test
    void execute_logsInUser_generatesJwt_andPublishesEvent() {
        //Mocks
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        JwtTokenProvider jwtToken = mock(JwtTokenProvider.class);

        UC2LoginUserUseCase sut = new UC2LoginUserUseCase(userRepo, passwordEncoder, publisher, jwtToken);

        // --- Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("jane@example.com");
        user.setUsername("janedoe");
        user.setPasswordHash("hashedPass");
        user.setRole(Role.RIDER);

        when(userRepo.findByUsernameOrEmail("janedoe", "janedoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPass")).thenReturn(true);
        when(jwtToken.generateToken(1L, "RIDER")).thenReturn("jwt-token-123");

        LoginRequest req = new LoginRequest("janedoe", "password123");

        // --- Act
        LoginResponse resp = sut.execute(req);

        // --- Assert
        assertThat(resp).isNotNull();
        assertThat(resp.token()).isEqualTo("jwt-token-123");
        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.role()).isEqualTo("RIDER");

        verify(publisher).publishEvent(isA(UserLoggedInEvent.class));
        verifyNoMoreInteractions(publisher);

        // --- Confirmation output
        System.out.println(" User logged in successfully:");
        System.out.println("   ID: " + resp.userId());
        System.out.println("   Role: " + resp.role());
        System.out.println("   JWT: " + resp.token());
    }

    @Test
    void execute_throwsIfPasswordInvalid() {
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        JwtTokenProvider jwtToken = mock(JwtTokenProvider.class);

        UC2LoginUserUseCase sut = new UC2LoginUserUseCase(userRepo, passwordEncoder, publisher, jwtToken);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("wrongHash");
        user.setRole(Role.RIDER);

        when(userRepo.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("badpassword", "wrongHash")).thenReturn(false);

        LoginRequest req = new LoginRequest("testuser", "badpassword");

        try {
            sut.execute(req);
            assertThat(true).as("Expected IllegalArgumentException").isFalse();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid password");
            System.out.println("Login failed as expected (invalid password).");
        }

        verifyNoInteractions(jwtToken);
        verifyNoInteractions(publisher);
    }
}
