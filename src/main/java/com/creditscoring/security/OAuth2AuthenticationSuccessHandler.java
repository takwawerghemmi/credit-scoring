package com.creditscoring.security;

import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.auth.service.RefreshTokenService;
import com.creditscoring.entity.Role;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        System.out.println("========== GOOGLE LOGIN SUCCESS ==========");

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String nom = oauthUser.getAttribute("family_name");
        String prenom = oauthUser.getAttribute("given_name");

        System.out.println("Google email = " + email);

        if (email == null || email.isBlank()) {
            response.sendRedirect(
                    "http://localhost:4200/login?error=google_email_missing"
            );
            return;
        }

        /*
         * Recherche de l'utilisateur
         */
        Utilisateur utilisateur =
                utilisateurRepository.findByEmail(email)
                        .orElse(null);

        /*
         * Création automatique du compte CLIENT
         * si l'utilisateur n'existe pas.
         */
        if (utilisateur == null) {

            Role role = roleRepository.findByNom("CLIENT")
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Rôle CLIENT introuvable"
                            )
                    );

            utilisateur = Utilisateur.builder()
                    .nom(nom != null ? nom : "")
                    .prenom(prenom != null ? prenom : "")
                    .email(email)
                    .motDePasse(UUID.randomUUID().toString())
                    .role(role)
                    .actif(true)
                    .build();

            utilisateur =
                    utilisateurRepository.save(utilisateur);

            System.out.println(
                    "Nouvel utilisateur Google créé : " + email
            );
        }

        /*
         * Création UserDetails
         */
        UserDetails userDetails =
                User.builder()
                        .username(utilisateur.getEmail())
                        .password(utilisateur.getMotDePasse())
                        .roles(utilisateur.getRole().getNom())
                        .build();

        /*
         * Génération JWT
         */
        String accessToken =
                jwtService.generateToken(userDetails);

        /*
         * Génération Refresh Token
         */
        RefreshToken refreshToken =
                refreshTokenService
                        .createRefreshToken(utilisateur);

        String refreshTokenValue =
                refreshToken.getToken();

        System.out.println(
                "Access token généré pour : " + email
        );

        /*
         * Encodage des valeurs avant redirection
         */
        String encodedAccessToken =
                URLEncoder.encode(
                        accessToken,
                        StandardCharsets.UTF_8
                );

        String encodedRefreshToken =
                URLEncoder.encode(
                        refreshTokenValue,
                        StandardCharsets.UTF_8
                );

        String encodedEmail =
                URLEncoder.encode(
                        email,
                        StandardCharsets.UTF_8
                );

        /*
         * Redirection vers Angular
         *
         * Le Frontend récupérera:
         *
         * token
         * refreshToken
         * email
         */
        String redirectUrl =
                "http://localhost:4200/login-success"
                        + "?token=" + encodedAccessToken
                        + "&refreshToken=" + encodedRefreshToken
                        + "&email=" + encodedEmail;

        response.sendRedirect(redirectUrl);

        System.out.println(
                "Redirection Google -> Angular : "
                        + redirectUrl
        );
    }
}