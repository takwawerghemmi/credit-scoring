package com.creditscoring.auth.service;

import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.auth.repository.RefreshTokenRepository;
import com.creditscoring.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional

@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(Utilisateur utilisateur) {

        refreshTokenRepository.deleteByUtilisateur(utilisateur);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expirationDate(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .utilisateur(utilisateur)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh Token introuvable"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh Token révoqué");
        }

        if (refreshToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh Token expiré");
        }

        return refreshToken;
    }

    public void revokeRefreshToken(Utilisateur utilisateur) {

        refreshTokenRepository.deleteByUtilisateur(utilisateur);

    }
}