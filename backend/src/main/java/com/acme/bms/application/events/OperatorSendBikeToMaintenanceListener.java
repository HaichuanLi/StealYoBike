package com.acme.bms.application.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OperatorSendBikeToMaintenanceListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(OperatorSendBikeToMaintenanceEvent event) {
        log.info("BikeSentToMaintenanceEvent: operatorId={}, bikeId={}",
                event.operatorId(), event.bikeId());
    }
}
