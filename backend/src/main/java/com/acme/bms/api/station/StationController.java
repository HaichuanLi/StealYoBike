package com.acme.bms.api.station;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.acme.bms.domain.repo.StationRepository;
import com.acme.bms.domain.entity.DockingStation;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
public class StationController {

    private final StationRepository stationRepository;
    private final com.acme.bms.api.station.StationSseService stationSseService;

    @Transactional(readOnly = true)
    @GetMapping("/{stationId}")
    public ResponseEntity<StationDetailResponse> getStationDetail(@PathVariable Long stationId) {
        Optional<DockingStation> station = stationRepository.findById(stationId);
        if (station.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new StationDetailResponse(station.get()));
    }

    @GetMapping("/list")
    public ResponseEntity<StationListResponse> getStationList() {
        return ResponseEntity.ok(StationListResponse.fromProjections(stationRepository.findAllStationSummaries()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStations() {
        return stationSseService.subscribe();
    }

}
