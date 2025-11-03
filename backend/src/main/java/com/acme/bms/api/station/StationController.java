package com.acme.bms.api.station;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.entity.DockingStation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
public class StationController {

    private final StationRepository stationRepository;

    @GetMapping("/detail")
    public ResponseEntity<StationDetailResponse> getStationDetail(@Valid @RequestBody Long stationId) {
        Optional<DockingStation> station = stationRepository.findById(stationId);
        if (station.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new StationDetailResponse(station.get()));
    }

    @GetMapping("/list")
    public ResponseEntity<StationListResponse> getStationList() {
        return ResponseEntity.ok(StationListResponse.fromEntities(stationRepository.findAll()));
    }

}
