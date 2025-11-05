package com.acme.bms.api.operator;

import com.acme.bms.application.usecase.UC5_RebalanceBikesUseCase;
import com.acme.bms.application.usecase.UC6_OperatorMarksStationOutOfService;
import com.acme.bms.application.usecase.UC7_OperatorSendBikeToMaintenance;

import com.acme.bms.application.usecase.UC8_RestoreInitialStateUseCase;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/operator")
@PreAuthorize("hasRole('OPERATOR')")
@RequiredArgsConstructor
public class OperatorController {

    private final UC5_RebalanceBikesUseCase uc5;
    private final UC6_OperatorMarksStationOutOfService uc6;
    private final UC7_OperatorSendBikeToMaintenance uc7;
    private final UC8_RestoreInitialStateUseCase uc8;

    @PostMapping("/rebalance")
    public ResponseEntity<RebalanceResponse> rebalance(@Valid @RequestBody RebalanceRequest request) {
        return ResponseEntity.ok(uc5.execute(request));
    }

    @PostMapping("/stations/out-of-service")
    public ResponseEntity<ChangeStationStateResponse> markOutOfService(
            @Valid @RequestBody ChangeStationStateRequest request) {

        return ResponseEntity.ok(uc6.execute(request));
    }

    @PostMapping("/bikes/maintenance")
    public ResponseEntity<OperatorSendBikeToMaintenanceResponse> sendBikeToMaintenance(
            @Valid @RequestBody OperatorSendBikeToMaintenanceRequest request) {
        return ResponseEntity.ok(uc7.execute(request));
    }

    @PostMapping("/restore-initial-state")
    public ResponseEntity<RestoreInitialStateResponse> restoreInitialState(
            @Valid @RequestBody RestoreInitialStateRequest request) {
        return ResponseEntity.ok(uc8.execute(request));
    }
}
