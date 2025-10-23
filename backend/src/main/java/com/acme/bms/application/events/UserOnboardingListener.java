package com.acme.bms.application.events;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserOnboardingListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserRegisteredEvent event) {

        log.info("UserRegisteredEvent: userId={}, role={}, email={}",
                event.userId(), event.role(), event.email());
    }

    @EventListener
    public void handle(UserLoggedInEvent event) {
        log.info("UserLoggedInEvent: userId={}, role={}, email={}",
                event.userId(), event.role(), event.email());
    }
}
