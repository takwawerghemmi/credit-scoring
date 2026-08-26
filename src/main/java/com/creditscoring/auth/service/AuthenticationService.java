package com.creditscoring.auth.service;

import com.creditscoring.auth.controller.CaptchaController;
import com.creditscoring.auth.dto.AuthenticationResponse;
import com.creditscoring.auth.dto.LoginRequest;
import com.creditscoring.auth.dto.RegisterRequest;
import com.creditscoring.auth.dto.RefreshTokenRequest;
import com.creditscoring.auth.dto.RefreshTokenResponse;
import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Role;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.ClientRepository;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final CaptchaController captchaController;
    private final RefreshTokenService refreshTokenService;
    private final UtilisateurRepository utilisateurRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    // =====================================================
    // LOGIN
    // =====================================================

    public AuthenticationResponse login(LoginRequest request) {

        if (request.getCaptchaId() == null ||
                request.getCaptchaCode() == null ||
                !captchaController.verifyCaptcha(
                        request.getCaptchaId(),
                        request.getCaptchaCode()
                )) {

            throw new RuntimeException(
                    "CAPTCHA incorrect ou expiré."
            );
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );

        UserDetails userDetails = User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().getNom())
                .build();

        String accessToken =
                jwtService.generateToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        utilisateur
                );

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().getNom())
                .build();
    }

    // =====================================================
    // REGISTER CLIENT
    // =====================================================

    public AuthenticationResponse register(
            RegisterRequest request
    ) {

        if (utilisateurRepository
                .findByEmail(request.getEmail())
                .isPresent()) {

            throw new RuntimeException(
                    "Email déjà utilisé"
            );
        }

        if (request.getCin() == null ||
                request.getCin().isBlank()) {

            throw new RuntimeException(
                    "Le CIN est obligatoire"
            );
        }

        if (request.getRevenuMensuel() == null) {

            throw new RuntimeException(
                    "Le revenu mensuel est obligatoire"
            );
        }

        Role role = roleRepository.findByNom("CLIENT")
                .orElseThrow(() ->
                        new RuntimeException(
                                "Rôle CLIENT introuvable"
                        )
                );

        Client client = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(
                        passwordEncoder.encode(
                                request.getMotDePasse()
                        )
                )
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .cin(request.getCin())
                .revenuMensuel(request.getRevenuMensuel())
                .role(role)
                .actif(false)
                .build();

        clientRepository.save(client);

        emailVerificationService.createToken(client);

        UserDetails userDetails = User.builder()
                .username(client.getEmail())
                .password(client.getMotDePasse())
                .roles(client.getRole().getNom())
                .build();

        String accessToken =
                jwtService.generateToken(userDetails);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(
                        client
                );

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(client.getEmail())
                .role(client.getRole().getNom())
                .build();
    }

    // =====================================================
    // REFRESH TOKEN
    // =====================================================

    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshToken(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.getRefreshToken()
                );

        Utilisateur utilisateur =
                refreshToken.getUtilisateur();

        UserDetails userDetails = User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().getNom())
                .build();

        String accessToken =
                jwtService.generateAccessToken(
                        userDetails
                );

        return RefreshTokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(
                        refreshToken.getToken()
                )
                .build();
    }

    // =====================================================
    // LOGOUT
    // =====================================================

    @Transactional
    public void logout(String refreshToken) {

        RefreshToken token =
                refreshTokenService.verifyRefreshToken(
                        refreshToken
                );

        refreshTokenService.revokeRefreshToken(
                token.getUtilisateur()
        );
    }
}