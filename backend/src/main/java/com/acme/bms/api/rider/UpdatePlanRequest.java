package com.acme.bms.api.rider;

import com.acme.bms.domain.entity.Plan;
import jakarta.validation.constraints.NotNull;

public record UpdatePlanRequest(
        @NotNull(message = "Plan is required") Plan plan) {
}
