package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import com.acme.bms.api.rider.ReserveBikeRequest;
import com.acme.bms.api.rider.ReserveBikeResponse;
import com.acme.bms.application.events.BikeReservedEvent;
import com.acme.bms.application.exception.NoAvailableBikesException;
import com.acme.bms.application.exception.ActiveReservationOrTripExistsException;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.UserRepository;
import com.acme.bms.domain.repo.TripRepository;

class UC3Test {

    @Test
    void execute_createsReservation_andPublishesEvent() {
        System.out.println("\n=== UC3: Happy path — reserve bike ===");
        System.out.println("[Before] Setting up mocks...");

        // Mocks
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        TripRepository tripRepo = mock(TripRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        StationRepository stationRepo = mock(StationRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        UC3_ReserveCheckoutUseCase sut = new UC3_ReserveCheckoutUseCase(reservationRepo, tripRepo, publisher,
                stationRepo,
                userRepo);

        ReserveBikeRequest req = new ReserveBikeRequest(100L, BikeType.ELECTRIC);

        // Mock user (use id lookup; UC3 uses findById)
        User rider = new User();
        rider.setId(1L);
        when(userRepo.findById(rider.getId())).thenReturn(Optional.of(rider));

        // Mock no existing reservation or trip
        when(reservationRepo.findByRiderIdAndStatus(rider.getId(), ReservationStatus.ACTIVE)).thenReturn(null);
        when(tripRepo.findByRiderIdAndStatus(rider.getId(), TripStatus.STARTED)).thenReturn(null);

        // Mock station + bike
        DockingStation station = mock(DockingStation.class);
        when(stationRepo.findById(100L)).thenReturn(Optional.of(station));
        when(station.getId()).thenReturn(100L);

        Bike bike = new Bike();
        bike.setId(42L);
        when(station.getFirstAvailableBike(BikeType.ELECTRIC)).thenReturn(bike);

        // Mock save reservation
        when(reservationRepo.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(999L);
            r.setPin("1234");
            r.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));
            return r;
        });

        System.out.println("[Action] Executing UC3 for user '" + rider.getId() + "'");
        ReserveBikeResponse resp = sut.execute(req, rider.getId());

        System.out.println("[After] Verifying results...");
        assertThat(resp.reservationId()).isEqualTo(999L);
        assertThat(resp.bikeId()).isEqualTo(42L);
        assertThat(resp.stationId()).isEqualTo(100L);
        assertThat(resp.pin()).isEqualTo("1234");
        assertThat(resp.expiresAt()).isNotNull();

        // Capture reservation to check content
        ArgumentCaptor<Reservation> cap = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepo).save(cap.capture());
        Reservation saved = cap.getValue();
        assertThat(saved.getBike().getId()).isEqualTo(42L);
        assertThat(saved.getRider().getId()).isEqualTo(1L); // ✅ fixed (was getUser)

        verify(publisher).publishEvent(isA(BikeReservedEvent.class));

        System.out.println("[Success] Reserved successfully: reservationId=" + resp.reservationId()
                + ", bikeId=" + resp.bikeId()
                + ", stationId=" + resp.stationId()
                + ", pin=" + resp.pin());
    }

    @Test
    void execute_throwsWhenNoAvailableBikes() {
        System.out.println("\n=== UC3: Error path — no available bikes ===");
        System.out.println("[Before] Setting up mocks...");

        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        TripRepository tripRepo = mock(TripRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        StationRepository stationRepo = mock(StationRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        UC3_ReserveCheckoutUseCase sut = new UC3_ReserveCheckoutUseCase(reservationRepo, tripRepo, publisher,
                stationRepo,
                userRepo);

        ReserveBikeRequest req = new ReserveBikeRequest(200L, BikeType.REGULAR); // ✅ changed from MECHANICAL

        User rider = new User();
        rider.setId(10L);
        when(userRepo.findById(rider.getId())).thenReturn(Optional.of(rider));

        // Mock no existing reservation or trip
        when(reservationRepo.findByRiderIdAndStatus(rider.getId(), ReservationStatus.ACTIVE)).thenReturn(null);
        when(tripRepo.findByRiderIdAndStatus(rider.getId(), TripStatus.STARTED)).thenReturn(null);

        DockingStation station = mock(DockingStation.class);
        when(stationRepo.findById(200L)).thenReturn(Optional.of(station));
        when(station.getFirstAvailableBike(BikeType.REGULAR)).thenReturn(null);

        System.out.println("[Action] Executing UC3 expecting NoAvailableBikesException...");
        NoAvailableBikesException ex = assertThrows(
                NoAvailableBikesException.class,
                () -> sut.execute(req, rider.getId()));

        System.out.println("[After] Exception caught successfully:");
        System.out.println("   Message: " + ex.getMessage());

        // Verify reservation lookup was performed but no save
        verify(reservationRepo).findByRiderIdAndStatus(rider.getId(), ReservationStatus.ACTIVE);
        verify(tripRepo).findByRiderIdAndStatus(rider.getId(), TripStatus.STARTED);
        verify(reservationRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void execute_throwsWhenRiderHasActiveReservation() {
        System.out.println("\n=== UC3: Error path — rider already has active reservation ===");
        System.out.println("[Before] Setting up mocks...");

        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        TripRepository tripRepo = mock(TripRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        StationRepository stationRepo = mock(StationRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        UC3_ReserveCheckoutUseCase sut = new UC3_ReserveCheckoutUseCase(reservationRepo, tripRepo, publisher,
                stationRepo,
                userRepo);

        ReserveBikeRequest req = new ReserveBikeRequest(200L, BikeType.REGULAR);

        User rider = new User();
        rider.setId(10L);
        when(userRepo.findById(rider.getId())).thenReturn(Optional.of(rider));

        // Mock existing active reservation
        Reservation existingReservation = mock(Reservation.class);
        when(reservationRepo.findByRiderIdAndStatus(rider.getId(), ReservationStatus.ACTIVE))
                .thenReturn(existingReservation);

        System.out.println("[Action] Executing UC3 expecting ActiveReservationOrTripExistsException...");
        ActiveReservationOrTripExistsException ex = assertThrows(
                ActiveReservationOrTripExistsException.class,
                () -> sut.execute(req, rider.getId()));

        System.out.println("[After] Exception caught successfully:");
        System.out.println("   Message: " + ex.getMessage());

        // Verify no station lookup was performed (early exit)
        verifyNoInteractions(stationRepo);
        verify(reservationRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void execute_throwsWhenRiderHasActiveTrip() {
        System.out.println("\n=== UC3: Error path — rider already has active trip ===");
        System.out.println("[Before] Setting up mocks...");

        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        TripRepository tripRepo = mock(TripRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        StationRepository stationRepo = mock(StationRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        UC3_ReserveCheckoutUseCase sut = new UC3_ReserveCheckoutUseCase(reservationRepo, tripRepo, publisher,
                stationRepo,
                userRepo);

        ReserveBikeRequest req = new ReserveBikeRequest(200L, BikeType.ELECTRIC);

        User rider = new User();
        rider.setId(10L);
        when(userRepo.findById(rider.getId())).thenReturn(Optional.of(rider));

        // Mock no active reservation but has an active trip
        when(reservationRepo.findByRiderIdAndStatus(rider.getId(), ReservationStatus.ACTIVE))
                .thenReturn(null);
        Trip existingTrip = mock(Trip.class);
        when(tripRepo.findByRiderIdAndStatus(rider.getId(), TripStatus.STARTED))
                .thenReturn(existingTrip);

        System.out.println("[Action] Executing UC3 expecting ActiveReservationOrTripExistsException...");
        ActiveReservationOrTripExistsException ex = assertThrows(
                ActiveReservationOrTripExistsException.class,
                () -> sut.execute(req, rider.getId()));

        System.out.println("[After] Exception caught successfully:");
        System.out.println("   Message: " + ex.getMessage());

        // Verify no station lookup was performed (early exit)
        verifyNoInteractions(stationRepo);
        verify(reservationRepo, never()).save(any());
        verify(publisher, never()).publishEvent(any());
    }
}
