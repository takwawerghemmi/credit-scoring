package com.creditscoring.auth.controller;

import com.creditscoring.auth.dto.AuthenticationResponse;
import com.creditscoring.auth.dto.LoginRequest;
import com.creditscoring.auth.dto.RefreshTokenRequest;
import com.creditscoring.auth.dto.RefreshTokenResponse;
import com.creditscoring.auth.dto.RegisterRequest;
import com.creditscoring.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authenticationService.login(request)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(
                authenticationService.register(request)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authenticationService.refreshToken(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody RefreshTokenRequest request) {

        authenticationService.logout(
                request.getRefreshToken()
        );

        return ResponseEntity.ok(
                "Déconnexion effectuée avec succès."
        );
    }
}