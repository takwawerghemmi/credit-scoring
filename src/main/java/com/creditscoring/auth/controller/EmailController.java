package com.creditscoring.auth.controller;

import com.creditscoring.auth.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ) {

        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(
                "Votre compte est maintenant activé."
        );
    }

    @PostMapping("/verify-email-code")
    public ResponseEntity<String> verifyEmailCode(
            @RequestBody VerifyEmailCodeRequest request
    ) {

        emailVerificationService.verifyEmailCode(
                request.getEmail(),
                request.getCode()
        );

        return ResponseEntity.ok(
                "Votre compte est maintenant activé."
        );
    }

    public static class VerifyEmailCodeRequest {

        private String email;
        private String code;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}