package com.creditscoring.auth.service;

import com.creditscoring.auth.dto.AuthenticationResponse;
import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.entity.Role;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.security.jwt.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    @Value("${google.client-id}")
    private String googleClientId;

    public AuthenticationResponse loginWithGoogle(String idToken) {

        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Google ID Token manquant");
        }

        try {

            // ==========================================
            // 1. Vérification du token auprès de Google
            // ==========================================

            HttpClient client = HttpClient.newHttpClient();

            String url =
                    "https://oauth2.googleapis.com/tokeninfo?id_token="
                            + URLEncoder.encode(
                                    idToken,
                                    StandardCharsets.UTF_8
                            );

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

            HttpResponse<String> response =
                    client.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Google ID Token invalide"
                );
            }

            JsonNode googleData =
                    objectMapper.readTree(response.body());

            // ==========================================
            // 2. Vérification du Client ID
            // ==========================================

            String audience =
                    googleData.path("aud").asText();

            if (!googleClientId.equals(audience)) {

                throw new RuntimeException(
                        "Google Client ID invalide"
                );
            }

            // ==========================================
            // 3. Récupération informations Google
            // ==========================================

            String email =
                    googleData.path("email").asText();

            String firstName =
                    googleData.path("given_name").asText("");

            String lastName =
                    googleData.path("family_name").asText("");

            if (email == null || email.isBlank()) {
                throw new RuntimeException(
                        "Email Google introuvable"
                );
            }

            // ==========================================
            // 4. Recherche utilisateur
            // ==========================================

            Utilisateur utilisateur =
                    utilisateurRepository
                            .findByEmail(email)
                            .orElse(null);

            // ==========================================
            // 5. Création automatique du CLIENT
            // ==========================================

            if (utilisateur == null) {

                Role role =
                        roleRepository
                                .findByNom("CLIENT")
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Rôle CLIENT introuvable"
                                        )
                                );

                utilisateur =
                        Utilisateur.builder()
                                .nom(lastName)
                                .prenom(firstName)
                                .email(email)
                                .motDePasse(
                                        UUID.randomUUID().toString()
                                )
                                .role(role)
                                .actif(true)
                                .build();

                utilisateur =
                        utilisateurRepository.save(
                                utilisateur
                        );
            }

            // ==========================================
            // 6. Vérification compte actif
            // ==========================================

            if (Boolean.FALSE.equals(
                    utilisateur.getActif()
            )) {

                throw new RuntimeException(
                        "Ce compte est désactivé"
                );
            }

            // ==========================================
            // 7. UserDetails
            // ==========================================

            UserDetails userDetails =
                    User.builder()
                            .username(
                                    utilisateur.getEmail()
                            )
                            .password(
                                    utilisateur.getMotDePasse()
                            )
                            .roles(
                                    utilisateur
                                            .getRole()
                                            .getNom()
                            )
                            .build();

            // ==========================================
            // 8. Génération JWT
            // ==========================================

            String accessToken =
                    jwtService.generateToken(
                            userDetails
                    );

            // ==========================================
            // 9. Génération Refresh Token
            // ==========================================

            RefreshToken refreshToken =
                    refreshTokenService
                            .createRefreshToken(
                                    utilisateur
                            );

            // ==========================================
            // 10. Réponse
            // ==========================================

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(
                            refreshToken.getToken()
                    )
                    .email(
                            utilisateur.getEmail()
                    )
                    .role(
                            utilisateur
                                    .getRole()
                                    .getNom()
                    )
                    .build();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Échec de la connexion Google : "
                            + e.getMessage(),
                    e
            );
        }
    }
}