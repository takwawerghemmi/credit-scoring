package com.creditscoring.auth.repository;

import com.creditscoring.auth.entity.PasswordResetToken;
import com.creditscoring.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUtilisateur(Utilisateur utilisateur);

}