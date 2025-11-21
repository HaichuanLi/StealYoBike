package com.acme.bms.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.acme.bms.domain.entity.Plan;
import com.acme.bms.domain.entity.Tier;

public record RegisterRequest(
                @NotBlank @Size(max = 120) String fullName,
                @NotBlank @Size(max = 255) String address,
                @NotBlank @Email @Size(max = 180) String email,
                @NotBlank @Size(min = 3, max = 100) String username,
                @NotBlank @Size(min = 8, max = 100) String password,
                @Size(max = 255) String paymentToken,
                Plan plan,
                Tier tier) {
}
