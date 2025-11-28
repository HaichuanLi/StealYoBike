package com.acme.bms.api.rider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.acme.bms.api.auth.UserInfoResponse;
import com.acme.bms.application.usecase.*;
import com.acme.bms.domain.entity.Plan;
import com.acme.bms.domain.entity.Role;
import com.acme.bms.domain.entity.Tier;
import com.acme.bms.domain.entity.User;
import com.acme.bms.domain.repo.DockingStationRepository;
import com.acme.bms.domain.repo.TripRepository;
import com.acme.bms.domain.repo.UserRepository;

@ExtendWith(MockitoExtension.class)
public class RiderControllerUpdatePlanTest {

    @Mock
    private UC3_ReserveCheckoutUseCase reserveUC;

    @Mock
    private UC4_ReturnBikeUseCase returnUC;

    @Mock
    private UC11_RiderCheckTripBill billUC;

    @Mock
    private UC13_ListPastTrips listPastTripsUC;

    @Mock
    private UC12_PayTripBill uc12;

    @Mock
    private UC15_GetTripDetails uc15;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DockingStationRepository stationRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private RiderController controller;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .address("123 Test St")
                .passwordHash("hashedpassword")
                .role(Role.RIDER)
                .plan(Plan.PAYPERRIDE)
                .tier(Tier.REGULAR)
                .flexDollar(0.0)
                .build();
    }

    @Test
    void updatePlan_shouldSuccessfullyUpdateToMonthly() {
        // Arrange
        String principal = "1";
        UpdatePlanRequest request = new UpdatePlanRequest(Plan.MONTHLY);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tripRepository.countByUserSince(any(Long.class), any(LocalDateTime.class))).thenReturn(5);
        when(tripRepository.countTripsPerMonth(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3);
        when(tripRepository.countTripsPerWeek(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(2);

        // Act
        ResponseEntity<UserInfoResponse> response = controller.updatePlan(principal, request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("MONTHLY", response.getBody().plan());
        assertEquals("testuser", response.getBody().username());
        assertEquals("test@example.com", response.getBody().email());

        // Verify the plan was set on the user
        verify(userRepository).save(any(User.class));
        assertEquals(Plan.MONTHLY, testUser.getPlan());
    }

    @Test
    void updatePlan_shouldSuccessfullyUpdateToAnnual() {
        // Arrange
        String principal = "1";
        UpdatePlanRequest request = new UpdatePlanRequest(Plan.ANNUAL);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tripRepository.countByUserSince(any(Long.class), any(LocalDateTime.class))).thenReturn(10);
        when(tripRepository.countTripsPerMonth(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5);
        when(tripRepository.countTripsPerWeek(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3);

        // Act
        ResponseEntity<UserInfoResponse> response = controller.updatePlan(principal, request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ANNUAL", response.getBody().plan());

        verify(userRepository).save(any(User.class));
        assertEquals(Plan.ANNUAL, testUser.getPlan());
    }

    @Test
    void updatePlan_shouldSuccessfullyUpdateBackToPayPerRide() {
        // Arrange
        testUser.setPlan(Plan.MONTHLY);
        String principal = "1";
        UpdatePlanRequest request = new UpdatePlanRequest(Plan.PAYPERRIDE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tripRepository.countByUserSince(any(Long.class), any(LocalDateTime.class))).thenReturn(2);
        when(tripRepository.countTripsPerMonth(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1);
        when(tripRepository.countTripsPerWeek(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1);

        // Act
        ResponseEntity<UserInfoResponse> response = controller.updatePlan(principal, request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("PAYPERRIDE", response.getBody().plan());

        verify(userRepository).save(any(User.class));
        assertEquals(Plan.PAYPERRIDE, testUser.getPlan());
    }

    @Test
    void updatePlan_shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        String principal = "999";
        UpdatePlanRequest request = new UpdatePlanRequest(Plan.MONTHLY);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            controller.updatePlan(principal, request);
        });
    }

    @Test
    void updatePlan_shouldReturnCorrectUserInfoWithAllFields() {
        // Arrange
        String principal = "1";
        UpdatePlanRequest request = new UpdatePlanRequest(Plan.MONTHLY);

        testUser.setPaymentToken("payment-token-123");
        testUser.setTier(Tier.GOLD);
        testUser.setFlexDollar(25.50);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tripRepository.countByUserSince(any(Long.class), any(LocalDateTime.class))).thenReturn(15);
        when(tripRepository.countTripsPerMonth(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(8);
        when(tripRepository.countTripsPerWeek(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(4);

        // Act
        ResponseEntity<UserInfoResponse> response = controller.updatePlan(principal, request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        UserInfoResponse userInfo = response.getBody();
        assertNotNull(userInfo);
        assertEquals(1L, userInfo.id());
        assertEquals("test@example.com", userInfo.email());
        assertEquals("testuser", userInfo.username());
        assertEquals("Test User", userInfo.fullName());
        assertEquals("RIDER", userInfo.role());
        assertEquals("MONTHLY", userInfo.plan());
        assertEquals("GOLD", userInfo.tier());
        assertEquals(25.50, userInfo.flexDollar());
        assertEquals(15, userInfo.tripsLastYear());
        assertEquals(8, userInfo.tripsLast3Months());
        assertEquals(4, userInfo.tripsLast12Weeks());
    }

    @Test
    void updatePlan_shouldWorkWithDifferentUserRoles() {
        // Arrange
        testUser.setRole(Role.OPERATOR);
        String principal = "1";
        UpdatePlanRequest request = new UpdatePlanRequest(Plan.ANNUAL);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tripRepository.countByUserSince(any(Long.class), any(LocalDateTime.class))).thenReturn(0);
        when(tripRepository.countTripsPerMonth(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);
        when(tripRepository.countTripsPerWeek(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0);

        // Act
        ResponseEntity<UserInfoResponse> response = controller.updatePlan(principal, request);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ANNUAL", response.getBody().plan());
        assertEquals("OPERATOR", response.getBody().role());
        assertEquals(true, response.getBody().dualRole());
    }
}
