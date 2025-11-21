package com.acme.bms.api.auth;

public record UserInfoResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String role,
        String paymentToken,
        String plan
) {}
