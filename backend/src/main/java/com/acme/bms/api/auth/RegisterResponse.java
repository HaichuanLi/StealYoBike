package com.acme.bms.api.auth;

public record RegisterResponse(
        Long id,
        String email,
        String username,
        String role
) {}
