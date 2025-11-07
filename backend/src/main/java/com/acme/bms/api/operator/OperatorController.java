package com.acme.bms.api.operator;

import com.acme.bms.application.usecase.UC5_RebalanceBikesUseCase;
import com.acme.bms.application.usecase.UC6_OperatorMarksStationOutOfService;
import com.acme.bms.application.usecase.UC7_OperatorSendBikeToMaintenance;
import com.acme.bms.application.usecase.UC8_RestoreInitialStateUseCase;
import com.acme.bms.application.usecase.UC14_ListAllUsersPastTrips;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import com.acme.bms.api.rider.PastTripResponse;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasRole('OPERATOR')")
@RequiredArgsConstructor
public class OperatorController {

    private final UC5_RebalanceBikesUseCase uc5;
    private final UC6_OperatorMarksStationOutOfService uc6;
    private final UC7_OperatorSendBikeToMaintenance uc7;
    private final UC8_RestoreInitialStateUseCase uc8;
    private final UC14_ListAllUsersPastTrips uc14;

    @PostMapping("/rebalance")
    public ResponseEntity<RebalanceResponse> rebalance(@AuthenticationPrincipal String principal,
            @Valid @RequestBody RebalanceRequest request) {
        Long operatorId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(uc5.execute(operatorId, request));
    }

    @PostMapping("/stations/out-of-service")
    public ResponseEntity<ChangeStationStateResponse> markOutOfService(
            @AuthenticationPrincipal String principal, @Valid @RequestBody ChangeStationStateRequest request) {
        Long operatorId = parsePrincipalToLong(principal);

        return ResponseEntity.ok(uc6.execute(operatorId, request));
    }

    @PostMapping("/bikes/maintenance")
    public ResponseEntity<OperatorSendBikeToMaintenanceResponse> sendBikeToMaintenance(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody OperatorSendBikeToMaintenanceRequest request) {
        Long operatorId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(uc7.execute(operatorId, request));
    }

    @PostMapping("/bikes/out-of-service")
    public ResponseEntity<OperatorSendBikeToMaintenanceResponse> toggleBikeStatus(
            @AuthenticationPrincipal String principal,
            @Valid @RequestBody OperatorSendBikeToMaintenanceRequest request) {
        Long operatorId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(uc7.execute(operatorId, request));
    }

    @PostMapping("/restore-initial-state")
    public ResponseEntity<RestoreInitialStateResponse> restoreInitialState(
            @AuthenticationPrincipal String principal, @Valid @RequestBody RestoreInitialStateRequest request) {
        Long operatorId = parsePrincipalToLong(principal);
        return ResponseEntity.ok(uc8.execute(operatorId, request));
    }

    @GetMapping("/trips/history")
    public ResponseEntity<List<PastTripResponse>> getAllPastTrips(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(required = false, name = "type") String bikeType) {
        return ResponseEntity.ok(uc14.execute(fromDate, toDate, bikeType));
    }

    private Long parsePrincipalToLong(Object principal) {
        if (principal == null) {
            throw new IllegalArgumentException("No authenticated principal available");
        }
        if (principal instanceof String) {
            return Long.valueOf((String) principal);
        } else if (principal instanceof Long) {
            return (Long) principal;
        }
        return Long.valueOf(principal.toString());
    }
}
