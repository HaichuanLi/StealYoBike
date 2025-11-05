package com.acme.bms.application.events;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.acme.bms.api.station.StationSseService;

@ExtendWith(MockitoExtension.class)
public class ChangeStationStatusListenerTest {

    @Mock
    StationSseService stationSseService;

    @InjectMocks
    ChangeStationStatusListener listener;

    @Test
    public void handle_changeStationStatus_calls_broadcast() {
        listener.handle(new ChangeStationStatusEvent(1L, com.acme.bms.domain.entity.Status.StationStatus.ACTIVE));
        verify(stationSseService).broadcastStations();
    }

    @Test
    public void handle_systemRestored_calls_broadcast() {
        listener.handle(new SystemRestoredEvent(1L, 1, 1, 1, java.time.Instant.now()));
        verify(stationSseService).broadcastStations();
    }

    @Test
    public void handle_stationsChanged_calls_broadcast() {
        listener.handle(new StationsChangedEvent());
        verify(stationSseService).broadcastStations();
    }
}
