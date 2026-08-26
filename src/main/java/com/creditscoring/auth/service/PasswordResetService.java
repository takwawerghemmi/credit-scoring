package com.creditscoring.auth.service;

import com.creditscoring.auth.entity.PasswordResetToken;
import com.creditscoring.auth.repository.PasswordResetTokenRepository;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetToken createToken(Utilisateur utilisateur) {

        passwordResetTokenRepository
                .findByUtilisateur(utilisateur)
                .ifPresent(passwordResetTokenRepository::delete);

        PasswordResetToken token = PasswordResetToken.builder()
                .utilisateur(utilisateur)
                .build();

        return passwordResetTokenRepository.save(token);

    }

    public PasswordResetToken verifyToken(String token) {

        PasswordResetToken passwordResetToken =
                passwordResetTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException("Token invalide"));

        if (passwordResetToken.getUsed()) {
            throw new RuntimeException("Token déjà utilisé");
        }

        if (passwordResetToken.isExpired()) {
            throw new RuntimeException("Token expiré");
        }

        return passwordResetToken;
    }

    public void resetPassword(
            String token,
            String newPassword) {

        PasswordResetToken passwordResetToken =
                verifyToken(token);

        Utilisateur utilisateur =
                passwordResetToken.getUtilisateur();

        utilisateur.setMotDePasse(
                passwordEncoder.encode(newPassword));

        utilisateurRepository.save(utilisateur);

        passwordResetToken.setUsed(true);

        passwordResetTokenRepository.save(passwordResetToken);

    }

}