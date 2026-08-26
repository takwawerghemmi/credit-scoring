package com.creditscoring.auth.controller;

import com.creditscoring.auth.dto.ForgotPasswordRequest;
import com.creditscoring.auth.dto.ResetPasswordRequest;
import com.creditscoring.auth.entity.PasswordResetToken;
import com.creditscoring.auth.service.PasswordResetService;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.UtilisateurRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PasswordController {

    private final PasswordResetService passwordResetService;
    private final UtilisateurRepository utilisateurRepository;

    // =====================================================
    // FORGOT PASSWORD - FRONTEND
    // =====================================================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(request.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        PasswordResetToken token =
                passwordResetService.createToken(utilisateur);

        return ResponseEntity.ok(
                "Token généré : " + token.getToken()
        );
    }

    // =====================================================
    // RESET PASSWORD - FRONTEND
    // =====================================================

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.ok(
                "Mot de passe réinitialisé avec succès."
        );
    }
}