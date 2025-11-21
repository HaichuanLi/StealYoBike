package com.acme.bms.application.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TierChangedEventListener {

    @EventListener
    public void handle(TierChangedEvent event) {
        System.out.println(
                "Tier changed for user " + event.userId() +
                        ": " + event.oldTier() + " → " + event.newTier()
        );
    }
}
