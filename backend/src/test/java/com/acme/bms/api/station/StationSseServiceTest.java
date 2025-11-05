package com.acme.bms.api.station;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.acme.bms.domain.entity.Status.StationStatus;
import com.acme.bms.domain.repo.StationRepository;

@ExtendWith(MockitoExtension.class)
public class StationSseServiceTest {

    @Mock
    StationRepository stationRepository;

    @InjectMocks
    StationSseService sseService;

    @Test
    public void subscribe_and_broadcast_should_not_throw() throws Exception {
        StationSummaryDto dto = new StationSummaryDto(1L, "Test Station", StationStatus.ACTIVE, 45.0, -73.0,
                "addr", 0L, 10L, 10);

        when(stationRepository.findAllStationSummaries()).thenReturn(List.of(dto));

        SseEmitter emitter = sseService.subscribe();
        assertNotNull(emitter);

        // Broadcast should not throw
        sseService.broadcastStations();
    }
}
