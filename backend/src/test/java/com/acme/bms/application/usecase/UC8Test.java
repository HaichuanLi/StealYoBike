package com.acme.bms.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import com.acme.bms.api.operator.RestoreInitialStateRequest;
import com.acme.bms.api.operator.RestoreInitialStateResponse;
import com.acme.bms.application.events.SystemRestoredEvent;
import com.acme.bms.application.exception.ForbiddenOperationException;
import com.acme.bms.application.exception.OperatorNotFoundException;
import com.acme.bms.domain.entity.Bike;
import com.acme.bms.domain.entity.BikeType;
import com.acme.bms.domain.entity.Dock;
import com.acme.bms.domain.entity.DockingStation;
import com.acme.bms.domain.entity.Reservation;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Trip;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.entity.Status.BikeStatus;
import com.acme.bms.domain.entity.Status.DockStatus;
import com.acme.bms.domain.entity.Status.ReservationStatus;
import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.entity.Status.TripStatus;
import com.acme.bms.domain.repo.BikeRepository;
import com.acme.bms.domain.repo.ReservationRepository;
import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.domain.repo.UserRepository;

class UC8Test {

        @Test
        void execute_restoresSystem_andPublishesEvent() {
                System.out.println("\n=== UC8: Happy path — restore initial system state ===");

                // --- Mocks and SUT ---
                UserRepository userRepository = mock(UserRepository.class);
                StationRepository stationRepository = mock(StationRepository.class);
                BikeRepository bikeRepository = mock(BikeRepository.class);
                ReservationRepository reservationRepository = mock(ReservationRepository.class);
                TripRepository tripRepository = mock(TripRepository.class);
                ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

                UC8_RestoreInitialStateUseCase useCase = new UC8_RestoreInitialStateUseCase(
                                userRepository, stationRepository, bikeRepository,
                                reservationRepository, tripRepository, eventPublisher);

                // --- Operator setup ---
                User operatorUser = new User();
                operatorUser.setId(99L);
                operatorUser.setRole(Role.OPERATOR);
                when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(operatorUser));

                // --- Domain setup: two stations, two docks each ---
                DockingStation stationA = new DockingStation();
                stationA.setId(1L);
                stationA.setStatus(StationStatus.OUT_OF_SERVICE); // Will be restored to ACTIVE

                DockingStation stationB = new DockingStation();
                stationB.setId(2L);
                stationB.setStatus(StationStatus.ACTIVE);

                Dock dockA1 = new Dock();
                dockA1.setId(11L);
                dockA1.setStation(stationA);

                Dock dockA2 = new Dock();
                dockA2.setId(12L);
                dockA2.setStation(stationA);

                Dock dockB1 = new Dock();
                dockB1.setId(21L);
                dockB1.setStation(stationB);

                Dock dockB2 = new Dock();
                dockB2.setId(22L);
                dockB2.setStation(stationB);

                Bike bikeA = Bike.builder()
                                .id(1001L)
                                .type(BikeType.REGULAR)
                                .status(BikeStatus.RESERVED) // Will be restored to AVAILABLE
                                .reservationExpiry(Instant.now().plusSeconds(300))
                                .build();
                bikeA.setState(new com.acme.bms.domain.entity.Status.BikeState.ReservedState(bikeA));

                Bike bikeB = Bike.builder()
                                .id(1002L)
                                .type(BikeType.ELECTRIC)
                                .status(BikeStatus.MAINTENANCE) // Will be restored to AVAILABLE
                                .maintenanceForced(true)
                                .build();
                bikeB.setState(new com.acme.bms.domain.entity.Status.BikeState.MaintenanceState(bikeB));

                // Station A: Dock A1 has bikeA; Dock A2 is empty
                dockA1.setBike(bikeA);
                bikeA.setDock(dockA1); // Proper association

                // Station B: Dock B1 has bikeB but with bad back reference
                dockB1.setBike(bikeB);
                bikeB.setDock(null); // Inconsistent state - will be fixed

                stationA.setDocks(new java.util.ArrayList<>(List.of(dockA1, dockA2)));
                stationB.setDocks(new java.util.ArrayList<>(List.of(dockB1, dockB2)));

                when(stationRepository.findAll()).thenReturn(List.of(stationA, stationB));
                when(stationRepository.save(any(DockingStation.class))).thenAnswer(i -> i.getArgument(0));
                when(bikeRepository.save(any(Bike.class))).thenAnswer(i -> i.getArgument(0));
                when(reservationRepository.findAll()).thenReturn(List.of());
                when(tripRepository.findAll()).thenReturn(List.of());

                // Before
                System.out.println("[Before Restore]");
                System.out.println("  Total stations: 2 (Station A: OUT_OF_SERVICE, Station B: ACTIVE)");
                System.out.println("  BikeA: status=" + bikeA.getStatus() + ", dock=" + (bikeA.getDock() != null));
                System.out.println("  BikeB: status=" + bikeB.getStatus() + ", dock=" + (bikeB.getDock() != null)
                                + ", maintenanceForced=" + bikeB.isMaintenanceForced());

                // Action
                System.out.println("\n[Action]");
                System.out.println("Executing UC8: restoring system to initial state for operator ID=99...");
                RestoreInitialStateRequest request = new RestoreInitialStateRequest();
                RestoreInitialStateResponse response = useCase.execute(99L, request);

                // After
                System.out.println("\n[After Restore]");
                System.out.println("  Stations restored: " + response.stations());
                System.out.println("  Docks restored:    " + response.docks());
                System.out.println("  Bikes restored:    " + response.bikes());
                System.out.println("  Message:           " + response.message());
                System.out.println("  Station A status:  " + stationA.getStatus());
                System.out.println("  BikeA: status=" + bikeA.getStatus() + ", dock=" + (bikeA.getDock() != null)
                                + ", expiry="
                                + bikeA.getReservationExpiry());
                System.out.println("  BikeB: status=" + bikeB.getStatus() + ", dock=" + (bikeB.getDock() != null)
                                + ", maintenanceForced=" + bikeB.isMaintenanceForced());

                // Assertions
                assertThat(response.stations()).isEqualTo(2);
                assertThat(response.docks()).isEqualTo(4);
                assertThat(response.bikes()).isEqualTo(2);

                // Verify stations are restored to ACTIVE
                assertThat(stationA.getStatus()).isEqualTo(StationStatus.ACTIVE);
                assertThat(stationB.getStatus()).isEqualTo(StationStatus.ACTIVE);

                // Verify dock statuses
                assertThat(dockA1.getStatus()).isEqualTo(DockStatus.OCCUPIED);
                assertThat(dockA2.getStatus()).isEqualTo(DockStatus.EMPTY);
                assertThat(dockB1.getStatus()).isEqualTo(DockStatus.OCCUPIED);
                assertThat(dockB2.getStatus()).isEqualTo(DockStatus.EMPTY);

                // Verify bike associations
                assertThat(bikeA.getDock()).isEqualTo(dockA1);
                assertThat(bikeB.getDock()).isEqualTo(dockB1); // Should be fixed

                // Verify bike states are restored
                assertThat(bikeA.getStatus()).isEqualTo(BikeStatus.AVAILABLE);
                assertThat(bikeB.getStatus()).isEqualTo(BikeStatus.AVAILABLE);
                assertThat(bikeA.getReservationExpiry()).isNull();
                assertThat(bikeB.isMaintenanceForced()).isFalse();

                // Verify event published
                verify(eventPublisher).publishEvent(isA(SystemRestoredEvent.class));
                verify(bikeRepository, times(2)).save(any(Bike.class));

                System.out.println("[Success] UC8 restored state; all bikes set to AVAILABLE, stations to ACTIVE.\n");
        }

        @Test
        void execute_throws_whenOperatorMissing() {
                System.out.println("\n=== UC8: Error path — operator not found ===");

                UserRepository userRepository = mock(UserRepository.class);
                StationRepository stationRepository = mock(StationRepository.class);
                BikeRepository bikeRepository = mock(BikeRepository.class);
                ReservationRepository reservationRepository = mock(ReservationRepository.class);
                TripRepository tripRepository = mock(TripRepository.class);
                ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

                UC8_RestoreInitialStateUseCase useCase = new UC8_RestoreInitialStateUseCase(
                                userRepository, stationRepository, bikeRepository,
                                reservationRepository, tripRepository, eventPublisher);

                when(userRepository.findById(404L)).thenReturn(java.util.Optional.empty());

                System.out.println("[Before] No user exists for operatorId=404");
                RestoreInitialStateRequest request = new RestoreInitialStateRequest();

                System.out.println("[Action] execute() expecting OperatorNotFoundException...");
                assertThrows(OperatorNotFoundException.class, () -> useCase.execute(404L, request));

                verifyNoInteractions(stationRepository, bikeRepository, reservationRepository, tripRepository,
                                eventPublisher);
                System.out.println("[Success] OperatorNotFoundException thrown; no state changes performed.\n");
        }

        @Test
        void execute_throws_whenUserIsNotOperator() {
                System.out.println("\n=== UC8: Error path — user is not an operator ===");

                UserRepository userRepository = mock(UserRepository.class);
                StationRepository stationRepository = mock(StationRepository.class);
                BikeRepository bikeRepository = mock(BikeRepository.class);
                ReservationRepository reservationRepository = mock(ReservationRepository.class);
                TripRepository tripRepository = mock(TripRepository.class);
                ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

                UC8_RestoreInitialStateUseCase useCase = new UC8_RestoreInitialStateUseCase(
                                userRepository, stationRepository, bikeRepository,
                                reservationRepository, tripRepository, eventPublisher);

                User riderUser = new User();
                riderUser.setId(12L);
                riderUser.setRole(Role.RIDER);
                when(userRepository.findById(12L)).thenReturn(java.util.Optional.of(riderUser));

                System.out.println("[Before] User 12 exists but role=RIDER");
                RestoreInitialStateRequest request = new RestoreInitialStateRequest();

                System.out.println("[Action] execute() expecting ForbiddenOperationException...");
                assertThrows(ForbiddenOperationException.class, () -> useCase.execute(12L, request));

                verifyNoInteractions(stationRepository, bikeRepository, reservationRepository, tripRepository,
                                eventPublisher);
                System.out.println("[Success] ForbiddenOperationException thrown; no state changes performed.\n");
        }

        @Test
        void execute_cancelsActiveReservationsAndTrips() {
                System.out.println("\n=== UC8: Cancels active reservations and completes active trips ===");

                // --- Mocks and SUT ---
                UserRepository userRepository = mock(UserRepository.class);
                StationRepository stationRepository = mock(StationRepository.class);
                BikeRepository bikeRepository = mock(BikeRepository.class);
                ReservationRepository reservationRepository = mock(ReservationRepository.class);
                TripRepository tripRepository = mock(TripRepository.class);
                ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

                UC8_RestoreInitialStateUseCase useCase = new UC8_RestoreInitialStateUseCase(
                                userRepository, stationRepository, bikeRepository,
                                reservationRepository, tripRepository, eventPublisher);

                // --- Operator setup ---
                User operatorUser = new User();
                operatorUser.setId(99L);
                operatorUser.setRole(Role.OPERATOR);
                when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(operatorUser));

                // --- Create active reservation and trip ---
                User rider = new User();
                rider.setId(10L);
                rider.setRole(Role.RIDER);

                Bike bike1 = Bike.builder()
                                .id(1001L)
                                .type(BikeType.REGULAR)
                                .status(BikeStatus.RESERVED)
                                .build();
                bike1.setState(new com.acme.bms.domain.entity.Status.BikeState.ReservedState(bike1));

                Bike bike2 = Bike.builder()
                                .id(1002L)
                                .type(BikeType.ELECTRIC)
                                .status(BikeStatus.ON_TRIP)
                                .build();
                bike2.setState(new com.acme.bms.domain.entity.Status.BikeState.OnTripState(bike2));

                // Mock reservation and trip since constructors are protected/complex
                Reservation activeReservation = mock(Reservation.class);
                when(activeReservation.getId()).thenReturn(1L);
                when(activeReservation.getRider()).thenReturn(rider);
                when(activeReservation.getBike()).thenReturn(bike1);
                // Important: getStatus() will be called multiple times - always return ACTIVE
                // until setStatus is called
                when(activeReservation.getStatus()).thenReturn(ReservationStatus.ACTIVE);

                Trip activeTrip = mock(Trip.class);
                when(activeTrip.getId()).thenReturn(2L);
                when(activeTrip.getRider()).thenReturn(rider);
                when(activeTrip.getBike()).thenReturn(bike2);
                // Important: getStatus() will be called multiple times - always return STARTED
                // until setStatus is called
                when(activeTrip.getStatus()).thenReturn(TripStatus.STARTED);

                // Create a simple station with bikes
                DockingStation station = new DockingStation();
                station.setId(1L);
                station.setStatus(StationStatus.ACTIVE);
                station.setDocks(new java.util.ArrayList<>());

                when(reservationRepository.findAll()).thenReturn(List.of(activeReservation));
                when(tripRepository.findAll()).thenReturn(List.of(activeTrip));
                when(stationRepository.findAll()).thenReturn(List.of(station));
                when(stationRepository.save(any(DockingStation.class))).thenAnswer(i -> i.getArgument(0));
                when(reservationRepository.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));
                when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));

                System.out.println("[Before Restore]");
                System.out.println("  Active reservation: " + activeReservation.getStatus());
                System.out.println("  Active trip: " + activeTrip.getStatus());

                // Action
                System.out.println("\n[Action]");
                RestoreInitialStateRequest request = new RestoreInitialStateRequest();
                useCase.execute(99L, request);

                System.out.println("\n[After Restore]");
                System.out.println("  Checking reservation and trip were processed...");

                // Verify the status was set
                verify(activeReservation).setStatus(ReservationStatus.CANCELLED);
                verify(activeTrip).setStatus(TripStatus.COMPLETED);

                // Verify they were saved
                verify(reservationRepository).save(activeReservation);
                verify(tripRepository).save(activeTrip);
                verify(eventPublisher).publishEvent(isA(SystemRestoredEvent.class));

                System.out.println("[Success] Active reservations cancelled and trips completed.\n");
        }

        @Test
        void execute_returnsOnTripBikesToDocks() {
                System.out.println("\n=== UC8: Returns ON_TRIP bikes to available docks ===");

                // --- Mocks and SUT ---
                UserRepository userRepository = mock(UserRepository.class);
                StationRepository stationRepository = mock(StationRepository.class);
                BikeRepository bikeRepository = mock(BikeRepository.class);
                ReservationRepository reservationRepository = mock(ReservationRepository.class);
                TripRepository tripRepository = mock(TripRepository.class);
                ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

                UC8_RestoreInitialStateUseCase useCase = new UC8_RestoreInitialStateUseCase(
                                userRepository, stationRepository, bikeRepository,
                                reservationRepository, tripRepository, eventPublisher);

                // --- Operator setup ---
                User operatorUser = new User();
                operatorUser.setId(99L);
                operatorUser.setRole(Role.OPERATOR);
                when(userRepository.findById(99L)).thenReturn(java.util.Optional.of(operatorUser));

                // --- Create bike on trip (no dock) ---
                User rider = new User();
                rider.setId(10L);
                rider.setRole(Role.RIDER);

                Bike onTripBike = Bike.builder()
                                .id(2001L)
                                .type(BikeType.ELECTRIC)
                                .status(BikeStatus.ON_TRIP)
                                .dock(null) // Bike is on trip, not in any dock
                                .build();
                onTripBike.setState(new com.acme.bms.domain.entity.Status.BikeState.OnTripState(onTripBike));

                Trip activeTrip = mock(Trip.class);
                when(activeTrip.getId()).thenReturn(100L);
                when(activeTrip.getRider()).thenReturn(rider);
                when(activeTrip.getBike()).thenReturn(onTripBike);
                when(activeTrip.getStatus()).thenReturn(TripStatus.STARTED);

                // --- Create station with empty dock ---
                DockingStation station = new DockingStation();
                station.setId(1L);
                station.setStatus(StationStatus.ACTIVE);

                Dock emptyDock = new Dock();
                emptyDock.setId(10L);
                emptyDock.setStation(station);
                emptyDock.setStatus(DockStatus.EMPTY);
                emptyDock.setBike(null);

                station.setDocks(new java.util.ArrayList<>(List.of(emptyDock)));

                when(reservationRepository.findAll()).thenReturn(List.of());
                when(tripRepository.findAll()).thenReturn(List.of(activeTrip));
                when(stationRepository.findAll()).thenReturn(List.of(station));
                when(stationRepository.save(any(DockingStation.class))).thenAnswer(i -> i.getArgument(0));
                when(tripRepository.save(any(Trip.class))).thenAnswer(i -> i.getArgument(0));
                when(bikeRepository.save(any(Bike.class))).thenAnswer(i -> i.getArgument(0));

                System.out.println("[Before Restore]");
                System.out.println("  Bike " + onTripBike.getId() + " status: " + onTripBike.getStatus());
                System.out.println("  Bike dock: " + onTripBike.getDock());
                System.out.println("  Empty dock " + emptyDock.getId() + " status: " + emptyDock.getStatus());

                // Action
                System.out.println("\n[Action]");
                RestoreInitialStateRequest request = new RestoreInitialStateRequest();
                useCase.execute(99L, request);

                System.out.println("\n[After Restore]");
                System.out.println("  Bike " + onTripBike.getId() + " dock: " + onTripBike.getDock());
                System.out.println("  Bike status: " + onTripBike.getStatus());
                System.out.println("  Bike state: " + onTripBike.getState());
                System.out.println("  Dock " + emptyDock.getId() + " status: " + emptyDock.getStatus());
                System.out.println(
                                "  Dock bike: " + (emptyDock.getBike() != null ? emptyDock.getBike().getId() : "null"));

                // Verify the bike was returned to the dock
                assertThat(onTripBike.getDock()).isEqualTo(emptyDock);
                assertThat(emptyDock.getBike()).isEqualTo(onTripBike);
                assertThat(emptyDock.getStatus()).isEqualTo(DockStatus.OCCUPIED);

                // Verify bike state was normalized
                assertThat(onTripBike.getStatus()).isEqualTo(BikeStatus.AVAILABLE);
                assertThat(onTripBike.getState())
                                .isInstanceOf(com.acme.bms.domain.entity.Status.BikeState.AvailableState.class);
                assertThat(onTripBike.getReservationExpiry()).isNull();
                assertThat(onTripBike.isMaintenanceForced()).isFalse();

                // Verify trip was completed
                verify(activeTrip).setStatus(TripStatus.COMPLETED);
                verify(tripRepository).save(activeTrip);
                verify(eventPublisher).publishEvent(isA(SystemRestoredEvent.class));

                System.out.println("[Success] ON_TRIP bike returned to empty dock.\n");
        }
}
