package com.acme.bms.api.station;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.acme.bms.domain.entity.DockingStation;
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
        DockingStation station = DockingStation.builder()
                .id(1L)
                .name("Test Station")
                .status(StationStatus.ACTIVE)
                .latitude(45.0)
                .longitude(-73.0)
                .streetAddress("addr")
                .capacity(10)
                .docks(Collections.emptyList())
                .build();

        when(stationRepository.findAll()).thenReturn(List.of(station));

        SseEmitter emitter = sseService.subscribe();
        assertNotNull(emitter);

        // Broadcast should not throw
        sseService.broadcastStations();
    }
}
