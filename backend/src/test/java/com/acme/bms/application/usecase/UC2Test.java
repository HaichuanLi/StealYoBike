package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.acme.bms.api.auth.LoginRequest;
import com.acme.bms.api.auth.LoginResponse;
import com.acme.bms.application.events.UserLoggedInEvent;
import com.acme.bms.application.exception.InvalidCredentialsException;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.infrastructure.security.JwtTokenProvider;

class UC2Test {

    @Test
    void execute_logsInUser_generatesJwt_andPublishesEvent() {
        System.out.println("\n=== UC2: Happy path — login ===");
        System.out.println("[Before] Setting up mocks...");

        // Mocks
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        JwtTokenProvider jwtToken = mock(JwtTokenProvider.class);

        UC2_LoginUserUseCase sut = new UC2_LoginUserUseCase(userRepo, passwordEncoder, publisher, jwtToken);

        // Arrange
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

        System.out.println("[Action] Executing UC2...");
        LoginResponse resp = sut.execute(req);

        System.out.println("[After] Verifying response...");
        assertThat(resp).isNotNull();
        assertThat(resp.token()).isEqualTo("jwt-token-123");
        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.role()).isEqualTo("RIDER");

        verify(publisher).publishEvent(isA(UserLoggedInEvent.class));
        verifyNoMoreInteractions(publisher);

        System.out.println("[Success] User logged in:");
        System.out.println("   ID: " + resp.userId());
        System.out.println("   Role: " + resp.role());
        System.out.println("   JWT: " + resp.token());
    }

    @Test
    void execute_throwsIfPasswordInvalid() {
        System.out.println("\n=== UC2: Error path — invalid password ===");
        System.out.println("[Before] Setting up mocks...");

        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        JwtTokenProvider jwtToken = mock(JwtTokenProvider.class);

        UC2_LoginUserUseCase sut = new UC2_LoginUserUseCase(userRepo, passwordEncoder, publisher, jwtToken);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("rightHash");
        user.setRole(Role.RIDER);

        when(userRepo.findByUsernameOrEmail("testuser", "testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("badpassword", "rightHash")).thenReturn(false);

        LoginRequest req = new LoginRequest("testuser", "badpassword");

        System.out.println("[Action] Executing UC2 expecting InvalidCredentialsException...");
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () -> sut.execute(req));

        System.out.println("[After] Exception caught: " + ex.getMessage());
        assertThat(ex.getMessage()).isEqualTo("Invalid username, email, or password");

        verifyNoInteractions(jwtToken);
        verifyNoInteractions(publisher);
    }
}
