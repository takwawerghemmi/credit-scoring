package com.creditscoring.auth.service;

import com.creditscoring.auth.entity.EmailVerificationToken;
import com.creditscoring.auth.repository.EmailVerificationTokenRepository;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailService emailService;

    public EmailVerificationToken createToken(
            Utilisateur utilisateur
    ) {

        tokenRepository.findByUtilisateur(utilisateur)
                .ifPresent(tokenRepository::delete);

        EmailVerificationToken token =
                EmailVerificationToken.builder()
                        .utilisateur(utilisateur)
                        .build();

        EmailVerificationToken savedToken =
                tokenRepository.save(token);

        String verificationUrl =
                "http://localhost:8082/api/auth/verify-email?token="
                        + savedToken.getToken();

        String contenu =
                "Bonjour "
                        + utilisateur.getPrenom()
                        + ",\n\n"
                        + "Bienvenue sur CreditNova.\n\n"
                        + "Votre compte a été créé avec succès.\n\n"
                        + "Votre code de vérification est : "
                        + savedToken.getCode()
                        + "\n\n"
                        + "Vous pouvez également cliquer sur ce lien :\n"
                        + verificationUrl
                        + "\n\n"
                        + "Ce code et ce lien sont valables pendant 24 heures.\n\n"
                        + "Cordialement,\n"
                        + "L'équipe CreditNova";

        emailService.envoyerEmail(
                utilisateur.getEmail(),
                "CreditNova - Vérification de votre email",
                contenu
        );

        return savedToken;
    }

    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Token invalide"
                                )
                        );

        verifierTokenDeBase(
                verificationToken
        );

        Utilisateur utilisateur =
                verificationToken.getUtilisateur();

        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);

        verificationToken.setVerified(true);
        tokenRepository.save(verificationToken);
    }

    public void verifyEmailCode(
            String email,
            String code
    ) {

        EmailVerificationToken verificationToken =
                tokenRepository
                        .findByUtilisateur_EmailAndCode(
                                email,
                                code
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Code de vérification invalide"
                                )
                        );

        verifierTokenDeBase(
                verificationToken
        );

        Utilisateur utilisateur =
                verificationToken.getUtilisateur();

        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);

        verificationToken.setVerified(true);
        tokenRepository.save(verificationToken);
    }

    private void verifierTokenDeBase(
            EmailVerificationToken verificationToken
    ) {

        if (verificationToken.getVerified()) {
            throw new RuntimeException(
                    "Email déjà vérifié"
            );
        }

        if (verificationToken.isExpired()) {
            throw new RuntimeException(
                    "Code ou lien de vérification expiré"
            );
        }
    }
}