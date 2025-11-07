package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.acme.bms.api.auth.UpdatePaymentTokenRequest;
import com.acme.bms.api.auth.UserInfoResponse;
import com.acme.bms.application.exception.UserNotFoundException;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.application.usecase.UC3_ReserveCheckoutUseCase;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.api.rider.ReserveBikeRequest;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.application.exception.PaymentMethodRequiredException;
import org.springframework.context.ApplicationEventPublisher;

class UC10Test {

    @Test
    void execute_updatesPaymentToken_andReturnsUserInfo() {
        System.out.println("\n=== TEST: Successful provide-payment flow ===");

        UserRepository userRepo = mock(UserRepository.class);

        UC10_ProvidePaymentMethod sut = new UC10_ProvidePaymentMethod(userRepo);

        User existing = User.builder()
                .id(42L)
                .email("bob@example.com")
                .username("bob")
                .fullName("Bob")
                .passwordHash("hash")
                .paymentToken(null)
                .build();

        when(userRepo.findById(42L)).thenReturn(Optional.of(existing));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdatePaymentTokenRequest req = new UpdatePaymentTokenRequest("tok-123");

        UserInfoResponse resp = sut.execute(req, 42L);

        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(42L);
        assertThat(resp.paymentToken()).isEqualTo("tok-123");

        // verify user saved with token
        verify(userRepo).save(any(User.class));
    }

    @Test
    void execute_throwsWhenUserMissing() {
        System.out.println("\n=== TEST: Provide payment with missing user ===");

        UserRepository userRepo = mock(UserRepository.class);
        UC10_ProvidePaymentMethod sut = new UC10_ProvidePaymentMethod(userRepo);

        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        UpdatePaymentTokenRequest req = new UpdatePaymentTokenRequest("tok-999");

        assertThrows(UserNotFoundException.class, () -> sut.execute(req, 99L));

        // also null id
        assertThrows(UserNotFoundException.class, () -> sut.execute(req, null));
    }

    @Test
    void reserve_throwsWhenPaymentTokenIsNull() {
    System.out.println("\n=== TEST: Reserve fails when payment token is null ===");

    ReservationRepository reservationRepo = mock(ReservationRepository.class);
    TripRepository tripRepo = mock(TripRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    StationRepository stationRepo = mock(StationRepository.class);
    UserRepository userRepo = mock(UserRepository.class);

    UC3_ReserveCheckoutUseCase sut = new UC3_ReserveCheckoutUseCase(
        reservationRepo, tripRepo, eventPublisher, stationRepo, userRepo);

    User rider = User.builder()
        .id(5L)
        .email("lihai@hotmail.com")
        .username("h1")
        .fullName("Li")
        .passwordHash("hash")
        .paymentToken(null)
        .build();

    when(userRepo.findById(5L)).thenReturn(Optional.of(rider));

    ReserveBikeRequest req = new ReserveBikeRequest(1L, BikeType.REGULAR);

    assertThrows(PaymentMethodRequiredException.class, () -> sut.execute(req, 5L));
    }

    @Test
    void reserve_throwsWhenPaymentTokenIsBlank() {
    System.out.println("\n=== TEST: Reserve fails when payment token is blank ===");

    ReservationRepository reservationRepo = mock(ReservationRepository.class);
    TripRepository tripRepo = mock(TripRepository.class);
    ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    StationRepository stationRepo = mock(StationRepository.class);
    UserRepository userRepo = mock(UserRepository.class);

    UC3_ReserveCheckoutUseCase sut = new UC3_ReserveCheckoutUseCase(
        reservationRepo, tripRepo, eventPublisher, stationRepo, userRepo);

    User rider = User.builder()
        .id(6L)
        .email("carl@example.com")
        .username("carl")
        .fullName("Carl")
        .passwordHash("hash")
        .paymentToken("   ")
        .build();

    when(userRepo.findById(6L)).thenReturn(Optional.of(rider));

    ReserveBikeRequest req = new ReserveBikeRequest(1L, BikeType.REGULAR);

    assertThrows(PaymentMethodRequiredException.class, () -> sut.execute(req, 6L));
    }
}
