package com.acme.bms.api.operator;

import com.acme.bms.application.usecase.UC5_RebalanceBikesUseCase;
import com.acme.bms.application.usecase.UC6_OperatorMarksStationOutOfService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/operator")
@RequiredArgsConstructor
public class OperatorController {

    private final UC5_RebalanceBikesUseCase uc5;
    private final UC6_OperatorMarksStationOutOfService uc6;

    @PostMapping("/rebalance")
    public ResponseEntity<RebalanceResponse> rebalance(@Valid @RequestBody RebalanceRequest request) {
        return ResponseEntity.ok(uc5.execute(request));
    }

    @PostMapping("/stations/out-of-service")
    public ResponseEntity<ChangeStationStateResponse> markOutOfService(@Valid @RequestBody ChangeStationStateRequest request) {
        return ResponseEntity.ok(uc6.execute(request));
    }
}
