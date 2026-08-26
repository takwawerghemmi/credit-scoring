package com.creditscoring.auth.repository;

import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUtilisateur(Utilisateur utilisateur);

    void deleteByUtilisateur(Utilisateur utilisateur);

}