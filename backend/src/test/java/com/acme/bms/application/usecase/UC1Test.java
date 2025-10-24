package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.acme.bms.api.auth.RegisterRequest;
import com.acme.bms.api.auth.RegisterResponse;
import com.acme.bms.application.events.UserRegisteredEvent;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;

class UC1Test {

    @Test
    void execute_registersUser_encodesPassword_andPublishesEvent() {
        //Mocks
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UC1_RegisterUserUseCase sut =
                new UC1_RegisterUserUseCase(userRepo, passwordEncoder, eventPublisher);

        //Arrange
        RegisterRequest req = new RegisterRequest(
                "Jane Doe",
                "456 Oak St",
                "jane@example.com",
                "janedoe",
                "password123",
                "token-xyz"
        );

        when(userRepo.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepo.existsByUsername("janedoe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed123");

        // simulate save() assigning id
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        //Act
        RegisterResponse resp = sut.execute(req);

        //Assert response
        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.email()).isEqualTo("jane@example.com");
        assertThat(resp.username()).isEqualTo("janedoe");
        assertThat(resp.role()).isEqualTo(Role.RIDER.name());

        //Verify password was encoded and saved user contains hash
        ArgumentCaptor<User> cap = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(cap.capture());
        User saved = cap.getValue();
        assertThat(saved.getPasswordHash()).isEqualTo("hashed123");
        assertThat(saved.getRole()).isEqualTo(Role.RIDER);

        //Verify event published
        verify(eventPublisher).publishEvent(isA(UserRegisteredEvent.class));
        verifyNoMoreInteractions(eventPublisher);

        //Confirmation output
        System.out.println(" User registered successfully:");
        System.out.println("   ID: " + resp.id());
        System.out.println("   Email: " + resp.email());
        System.out.println("   Username: " + resp.username());
        System.out.println("   Role: " + resp.role());
    }

    @Test
    void execute_throwsIfEmailExists() {
        UserRepository userRepo = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        UC1_RegisterUserUseCase sut =
                new UC1_RegisterUserUseCase(userRepo, passwordEncoder, eventPublisher);

        when(userRepo.existsByEmail("dup@example.com")).thenReturn(true);

        RegisterRequest req = new RegisterRequest(
                "Dup User", "Addr", "dup@example.com", "dup", "pass", null);

        try {
            sut.execute(req);
            assertThat(true).as("Expected exception").isFalse();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Email already in use");
        }

        verifyNoInteractions(passwordEncoder, eventPublisher);


    }
}
