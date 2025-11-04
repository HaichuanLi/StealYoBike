package com.acme.bms.api.auth;

import java.net.URI;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.bms.application.usecase.UC1_RegisterUserUseCase;
import com.acme.bms.application.usecase.UC2_LoginUserUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UC1_RegisterUserUseCase registerUC;
    private final UC2_LoginUserUseCase loginUC;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        var out = registerUC.execute(request);
        return ResponseEntity.created(URI.create("/api/users/" + out.id())).body(out); // 201 + Location
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUC.execute(request));
    }

    //Helps check if the token is still valid and fetch the user’s info without logging in again.
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return Map.of("username", user != null ? user.getUsername() : null);
    }
}
