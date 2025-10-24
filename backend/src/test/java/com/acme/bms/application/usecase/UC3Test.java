package com.acme.bms.application.usecase;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;

import com.acme.bms.api.rider.ReserveBikeRequest;
import com.acme.bms.api.rider.ReserveBikeResponse;
import com.acme.bms.application.events.BikeReservedEvent;
import com.acme.bms.domain.entity.*;
import com.acme.bms.domain.repo.*;

class UC3Test {

    @Test
    void execute_createsReservation_andPublishesEvent() {
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        StationRepository stationRepo = mock(StationRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        UC3_ReserveCheckoutUseCase sut =
                new UC3_ReserveCheckoutUseCase(reservationRepo, publisher, stationRepo);

        User user = new User(); user.setId(1L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        DockingStation station = mock(DockingStation.class);
        when(stationRepo.findById(100L)).thenReturn(Optional.of(station));
        when(station.getId()).thenReturn(100L);
        Bike bike = new Bike(); bike.setId(42L);
        when(station.getFirstAvailableBike(BikeType.ELECTRIC)).thenReturn(bike);

        when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(999L);
            r.setPin("1234");
            r.setExpiresAt(java.time.Instant.now().plus(15, java.time.temporal.ChronoUnit.MINUTES));
            return r;
        });

        ReserveBikeResponse resp = sut.execute(new ReserveBikeRequest(100L, BikeType.ELECTRIC));

        assertThat(resp.reservationId()).isEqualTo(999L);
        assertThat(resp.bikeId()).isEqualTo(42L);
        assertThat(resp.stationId()).isEqualTo(100L);
        assertThat(resp.pin()).isEqualTo("1234");

        verify(reservationRepo).save(any(Reservation.class));
        verify(publisher).publishEvent(isA(BikeReservedEvent.class));
    }
}
