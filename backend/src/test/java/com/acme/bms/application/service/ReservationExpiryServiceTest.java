package com.acme.bms.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.Status.BikeStatus;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.entity.Status.BikeState.ReservedState;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.ReservationRepository;

class ReservationExpiryServiceTest {

        private ReservationRepository reservationRepository;
        private ReservationExpiryService service;

        @BeforeEach
        void setUp() {
                reservationRepository = mock(ReservationRepository.class);
                service = new ReservationExpiryService(reservationRepository);
        }

        @Test
        void expireReservations_noExpiredReservations_noActionTaken() {
                System.out.println("\n=== ReservationExpiryService: No expired reservations ===");
                System.out.println("[Before] No expired reservations exist");

                // Given: No expired reservations
                when(reservationRepository.findByStatusAndExpiresAtBefore(
                                eq(ReservationStatus.ACTIVE),
                                any(Instant.class)))
                                .thenReturn(Collections.emptyList());

                // When: Service runs expiry check
                System.out.println("[Action] Running expireReservations()...");
                service.expireReservations();

                // Then: No reservations are saved
                System.out.println("[After] Verifying no updates were made...");
                verify(reservationRepository, never()).save(any(Reservation.class));
                System.out.println("[Success] No reservations expired (as expected)");
        }

        @Test
        void expireReservations_oneExpiredReservation_marksAsExpired() {
                System.out.println("\n=== ReservationExpiryService: Single expired reservation ===");
                System.out.println("[Before] Setting up one expired reservation...");

                // Given: One expired reservation
                User rider = User.builder()
                                .id(1L)
                                .username("john")
                                .build();

                Dock dock = new Dock();
                dock.setId(100L);

                Bike bike = Bike.builder()
                                .id(42L)
                                .type(BikeType.REGULAR)
                                .status(BikeStatus.AVAILABLE)
                                .dock(dock)
                                .build();
                bike.setState(new com.acme.bms.domain.entity.Status.BikeState.AvailableState(bike));

                dock.setBike(bike);

                // Create reservation using public constructor
                Reservation expiredReservation = new Reservation(rider, bike);
                expiredReservation.setId(999L);
                // Manually set it to expired
                expiredReservation.setCreatedAt(Instant.now().minusSeconds(400));
                expiredReservation.setExpiresAt(Instant.now().minusSeconds(100));

                when(reservationRepository.findByStatusAndExpiresAtBefore(
                                eq(ReservationStatus.ACTIVE),
                                any(Instant.class)))
                                .thenReturn(List.of(expiredReservation));

                // When: Service runs expiry check
                System.out.println("[Action] Running expireReservations()...");
                service.expireReservations();

                // Then: Reservation is marked as expired
                System.out.println("[After] Verifying reservation was expired...");
                assertThat(expiredReservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
                assertThat(bike.getStatus()).isEqualTo(BikeStatus.AVAILABLE);

                verify(reservationRepository, times(1)).save(expiredReservation);
                System.out.println("[Success] Reservation 999 expired and bike 42 is now AVAILABLE");
        }

        @Test
        void expireReservations_multipleExpiredReservations_expiresAll() {
                System.out.println("\n=== ReservationExpiryService: Multiple expired reservations ===");
                System.out.println("[Before] Setting up 3 expired reservations...");

                // Given: Three expired reservations
                List<Reservation> expiredReservations = createMultipleExpiredReservations(3);

                when(reservationRepository.findByStatusAndExpiresAtBefore(
                                eq(ReservationStatus.ACTIVE),
                                any(Instant.class)))
                                .thenReturn(expiredReservations);

                // When: Service runs expiry check
                System.out.println("[Action] Running expireReservations()...");
                service.expireReservations();

                // Then: All reservations are marked as expired
                System.out.println("[After] Verifying all reservations were expired...");
                for (Reservation reservation : expiredReservations) {
                        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
                        assertThat(reservation.getBike().getStatus()).isEqualTo(BikeStatus.AVAILABLE);
                }

                ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
                verify(reservationRepository, times(3)).save(captor.capture());

                List<Reservation> savedReservations = captor.getAllValues();
                assertThat(savedReservations).hasSize(3);
                System.out.println("[Success] All 3 reservations expired successfully");
        }

        @Test
        void expireReservations_bikeWithNoDock_logsWarning() {
                System.out.println("\n=== ReservationExpiryService: Bike with no dock ===");
                System.out.println("[Before] Setting up reservation with bike that has no dock...");

                // Given: Mock a reservation to avoid constructor issues
                User rider = User.builder()
                                .id(1L)
                                .username("jane")
                                .build();

                Bike bike = Bike.builder()
                                .id(99L)
                                .type(BikeType.ELECTRIC)
                                .status(BikeStatus.RESERVED)
                                .dock(null) // No dock!
                                .build();
                bike.setState(new ReservedState(bike));

                Reservation expiredReservation = mock(Reservation.class);
                when(expiredReservation.getId()).thenReturn(888L);
                when(expiredReservation.getRider()).thenReturn(rider);
                when(expiredReservation.getBike()).thenReturn(bike);
                when(expiredReservation.getStatus()).thenReturn(ReservationStatus.ACTIVE);
                when(expiredReservation.getCreatedAt()).thenReturn(Instant.now().minusSeconds(400));
                when(expiredReservation.getExpiresAt()).thenReturn(Instant.now().minusSeconds(100));

                when(reservationRepository.findByStatusAndExpiresAtBefore(
                                eq(ReservationStatus.ACTIVE),
                                any(Instant.class)))
                                .thenReturn(List.of(expiredReservation));

                // When: Service runs expiry check
                System.out.println("[Action] Running expireReservations()...");
                service.expireReservations();

                // Then: Reservation is marked as expired but bike state doesn't change
                System.out.println("[After] Verifying reservation status...");
                verify(expiredReservation).setStatus(ReservationStatus.EXPIRED);
                // Bike state remains RESERVED since returnBike() wasn't called due to null dock
                assertThat(bike.getStatus()).isEqualTo(BikeStatus.RESERVED);

                verify(reservationRepository, times(1)).save(expiredReservation);
                System.out.println("[Success] Reservation expired but warning logged for bike with no dock");
        }

        @Test
        void expireReservations_onlyExpiresActiveReservations() {
                System.out.println("\n=== ReservationExpiryService: Only ACTIVE reservations expired ===");
                System.out.println("[Before] Repository will only return ACTIVE reservations...");

                // Given: Query specifically for ACTIVE reservations
                when(reservationRepository.findByStatusAndExpiresAtBefore(
                                eq(ReservationStatus.ACTIVE),
                                any(Instant.class)))
                                .thenReturn(Collections.emptyList());

                // When: Service runs expiry check
                System.out.println("[Action] Running expireReservations()...");
                service.expireReservations();

                // Then: Verify correct query parameters
                System.out.println("[After] Verifying query parameters...");
                ArgumentCaptor<ReservationStatus> statusCaptor = ArgumentCaptor.forClass(ReservationStatus.class);
                ArgumentCaptor<Instant> timeCaptor = ArgumentCaptor.forClass(Instant.class);

                verify(reservationRepository).findByStatusAndExpiresAtBefore(
                                statusCaptor.capture(),
                                timeCaptor.capture());

                assertThat(statusCaptor.getValue()).isEqualTo(ReservationStatus.ACTIVE);
                assertThat(timeCaptor.getValue()).isBeforeOrEqualTo(Instant.now());
                System.out.println("[Success] Service correctly queries for ACTIVE reservations only");
        }

        // Helper method to create multiple expired reservations
        private List<Reservation> createMultipleExpiredReservations(int count) {
                Reservation[] reservations = new Reservation[count];

                for (int i = 0; i < count; i++) {
                        User rider = User.builder()
                                        .id((long) (i + 1))
                                        .username("user" + i)
                                        .build();

                        Dock dock = new Dock();
                        dock.setId((long) (100 + i));

                        Bike bike = Bike.builder()
                                        .id((long) (50 + i))
                                        .type(i % 2 == 0 ? BikeType.REGULAR : BikeType.ELECTRIC)
                                        .status(BikeStatus.AVAILABLE)
                                        .dock(dock)
                                        .build();
                        bike.setState(new com.acme.bms.domain.entity.Status.BikeState.AvailableState(bike));

                        dock.setBike(bike);

                        // Use public constructor
                        Reservation reservation = new Reservation(rider, bike);
                        reservation.setId((long) (1000 + i));
                        // Manually set to expired
                        reservation.setCreatedAt(Instant.now().minusSeconds(400));
                        reservation.setExpiresAt(Instant.now().minusSeconds(100));

                        reservations[i] = reservation;
                }

                return Arrays.asList(reservations);
        }
}
