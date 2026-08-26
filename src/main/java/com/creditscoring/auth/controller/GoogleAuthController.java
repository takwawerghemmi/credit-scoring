package com.creditscoring.auth.controller;

import com.creditscoring.auth.dto.AuthenticationResponse;
import com.creditscoring.auth.service.GoogleAuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    @PostMapping("/google-login")
    public ResponseEntity<AuthenticationResponse> googleLogin(
            @RequestBody GoogleLoginRequest request) {

        if (request.getIdToken() == null ||
                request.getIdToken().isBlank()) {

            throw new RuntimeException(
                    "Google ID Token manquant"
            );
        }

        AuthenticationResponse response =
                googleAuthService.loginWithGoogle(
                        request.getIdToken()
                );

        return ResponseEntity.ok(response);
    }

    @Data
    public static class GoogleLoginRequest {

        private String idToken;
    }
}