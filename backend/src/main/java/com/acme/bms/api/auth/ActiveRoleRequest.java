package com.acme.bms.api.auth;

import jakarta.validation.constraints.NotBlank;

public record ActiveRoleRequest(
        @NotBlank String role
) {
}
