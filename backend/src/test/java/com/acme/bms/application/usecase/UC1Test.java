package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.acme.bms.api.auth.RegisterRequest;
import com.acme.bms.domain.entity.Plan;
import com.acme.bms.api.auth.RegisterResponse;
import com.acme.bms.application.events.UserRegisteredEvent;
import com.acme.bms.application.exception.EmailAlreadyUsedException;
import com.acme.bms.application.exception.UsernameAlreadyUsedException;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;

class UC1Test {

    @Test
    void execute_registersUser_encodesPassword_andPublishesEvent() {
        System.out.println("\n=== TEST: Successful registration flow ===");
        System.out.println("[Before] Setting up mocks and preparing RegisterRequest...");

        // Mocks
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        UC1_RegisterUserUseCase sut = new UC1_RegisterUserUseCase(userRepo, passwordEncoder, eventPublisher);

        // Arrange
        RegisterRequest req = new RegisterRequest(
                "Jane Doe",
                "456 Oak St",
                "jane@example.com",
                "janedoe",
                "password123",
                "token-xyz",
                Plan.PAYPERRIDE);

        when(userRepo.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("janedoe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed123");

        // simulate save() assigning id
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        System.out.println("[Action] Executing use case...");
        RegisterResponse resp = sut.execute(req);

        System.out.println("[After] Verifying results and expectations...");

        // Assert response
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.email()).isEqualTo("jane@example.com");
        assertThat(resp.username()).isEqualTo("janedoe");
        assertThat(resp.role()).isEqualTo(Role.RIDER.name());

        // Verify password was encoded and saved user contains hash
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        User saved = cap.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed123");
        assertThat(saved.getRole()).isEqualTo(Role.RIDER);

        // Verify event published
        verify(eventPublisher).publishEvent(isA(UserRegisteredEvent.class));
        verifyNoMoreInteractions(eventPublisher);

        System.out.println("[Success] User registered successfully:");
        System.out.println("   ID: " + resp.id());
        System.out.println("   Email: " + resp.email());
        System.out.println("   Username: " + resp.username());
        System.out.println("   Role: " + resp.role());
    }

    @Test
    void execute_throwsIfEmailExists() {
        System.out.println("\n=== TEST: Email already in use ===");
        System.out.println("[Before] Creating mock repository that reports duplicate email...");

        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        UC1_RegisterUserUseCase sut = new UC1_RegisterUserUseCase(userRepo, passwordEncoder, eventPublisher);

        when(userRepo.existsByEmail("dup@example.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest(
                "Dup User", "Addr", "dup@example.com", "dup", "passw0rd!", null, Plan.PAYPERRIDE);

        System.out.println("[Action] Executing use case expecting EmailAlreadyUsedException...");
        EmailAlreadyUsedException ex = assertThrows(EmailAlreadyUsedException.class, () -> sut.execute(req));

        System.out.println("[After] Exception caught successfully:");
        System.out.println("   Message: " + ex.getMessage());

        assertThat(ex.getMessage()).isEqualTo("Email already in use");
        verifyNoInteractions(passwordEncoder, eventPublisher);
        verify(userRepo, never()).save(any());
    }

    @Test
    void execute_throwsIfUsernameExists() {
        System.out.println("\n=== TEST: Username already in use ===");
        System.out.println("[Before] Creating mock repository that reports duplicate username...");

        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
        UC1_RegisterUserUseCase sut = new UC1_RegisterUserUseCase(userRepo, passwordEncoder, eventPublisher);

        when(userRepo.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("dupuser")).thenReturn(true);

        RegisterRequest req = new RegisterRequest(
                "Dup User", "Addr", "new@example.com", "dupuser", "passw0rd!", null, Plan.PAYPERRIDE);

        System.out.println("[Action] Executing use case expecting UsernameAlreadyUsedException...");
        UsernameAlreadyUsedException ex = assertThrows(UsernameAlreadyUsedException.class, () -> sut.execute(req));

        System.out.println("[After] Exception caught successfully:");
        System.out.println("   Message: " + ex.getMessage());

        assertThat(ex.getMessage()).isEqualTo("Username already in use");
        verifyNoInteractions(passwordEncoder, eventPublisher);
        verify(userRepo, never()).save(any());
    }
}
