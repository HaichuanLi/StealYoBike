package com.acme.bms.application.service;

import com.acme.bms.domain.entity.Tier;

public interface TierEvaluationService {
    Tier evaluate(Long userId);
}
