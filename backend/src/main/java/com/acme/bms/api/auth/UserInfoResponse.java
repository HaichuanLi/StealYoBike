package com.acme.bms.api.auth;

public record UserInfoResponse(
        Long id,
        String email,
        String username,
        String fullName,
        String role,
        String activeRole,
        boolean dualRole,
        String paymentToken,
        String plan,
        String tier,
        double flexDollar,
        int tripsLastYear,
        int tripsLast3Months,
        int tripsLast12Weeks
) {}

