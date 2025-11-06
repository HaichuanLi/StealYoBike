package com.acme.bms.application.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.acme.bms.api.station.StationSseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeStationStatusListener {

    private final StationSseService stationSseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChangeStationStatusEvent event) {
        log.info("ChangeStationStatusEvent: stationId={}, stationStatus={}",
                event.stationId(), event.status());
        // Broadcast a fresh snapshot to connected clients
        stationSseService.broadcastStations();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SystemRestoredEvent event) {
        log.info("SystemRestoredEvent: stations={}, docks={}, bikes={}",
                event.stations(), event.docks(), event.bikes());
        stationSseService.broadcastStations();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(StationsChangedEvent event) {
        log.info("StationsChangedEvent: broadcasting snapshot");
        stationSseService.broadcastStations();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BikeReservedEvent event) {
        log.info("BikeReservedEvent: reservationId={}, userId={}, bikeId={}",
                event.reservationId(), event.userId(), event.bikeId());
        stationSseService.broadcastStations();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(BikeCheckedOutEvent event) {
        log.info("BikeCheckedOutEvent: tripId={}, userId={}, bikeId={}, stationId={}",
                event.tripId(), event.userId(), event.bikeId(), event.stationId());
        stationSseService.broadcastStations();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OperatorSendBikeToMaintenanceEvent event) {
        log.info("OperatorSendBikeToMaintenanceEvent: operatorId={}, bikeId={}",
                event.operatorId(), event.bikeId());
        stationSseService.broadcastStations();
    }
}
