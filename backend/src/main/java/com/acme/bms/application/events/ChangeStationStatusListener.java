package com.acme.bms.application.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChangeStationStatusListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChangeStationStatusEvent event) {
        log.info("ChangeStationStatusEvent: stationId={}, stationStatus={}",
                 event.stationId(), event.status());
    }
}