package com.acme.bms.api.auth;

public record LoginResponse(
        String token,
        Long userId,
        String role
) {}
