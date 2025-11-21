package com.acme.bms.application.events;

import com.acme.bms.domain.entity.Tier;

public record TierChangedEvent(
        Long userId,
        Tier oldTier,
        Tier newTier
) {}
