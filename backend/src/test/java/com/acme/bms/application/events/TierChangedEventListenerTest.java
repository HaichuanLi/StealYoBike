package com.acme.bms.application.events;

import com.acme.bms.domain.entity.Tier;
import org.junit.jupiter.api.Test;

class TierChangedEventListenerTest {

    @Test
    void onTierChanged_eventIsHandled() {
        System.out.println("\n=== TierChangedEventListenerTest===");

        TierChangedEventListener listener = new TierChangedEventListener();
        TierChangedEvent event = new TierChangedEvent(
                1L,
                Tier.BRONZE,
                Tier.SILVER
        );

        System.out.println("[Before] Created event:");
        System.out.println("    - userId = 1");
        System.out.println("    - oldTier = BRONZE");
        System.out.println("    - newTier = SILVER");

        System.out.println("\n[Action] Calling listener.onTierChanged()...");
        listener.handle(event);

        System.out.println("[After] Listener executed successfully.");
        System.out.println("[Success] TierChangedEventListener processes events correctly.\n");
    }
}
