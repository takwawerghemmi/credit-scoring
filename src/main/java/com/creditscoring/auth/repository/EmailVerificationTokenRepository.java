package com.creditscoring.auth.repository;

import com.creditscoring.auth.entity.EmailVerificationToken;
import com.creditscoring.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken>
    findByUtilisateur(Utilisateur utilisateur);

    Optional<EmailVerificationToken>
    findByToken(String token);

    Optional<EmailVerificationToken>
    findByUtilisateur_EmailAndCode(
            String email,
            String code
    );
}