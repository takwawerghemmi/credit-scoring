package com.creditscoring.auth.service;

import com.creditscoring.auth.dto.AuthenticationResponse;
import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.security.jwt.JwtService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FaceAuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    /*
     * Distance maximale acceptée entre deux
     * face descriptors.
     *
     * 0.6 est une valeur de départ classique
     * pour les descriptors face-api.js.
     */
    private static final double FACE_THRESHOLD = 0.60;

    // =====================================================
    // REGISTER FACE
    // =====================================================

    public void registerFace(
            String email,
            String faceImage) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException(
                    "Email obligatoire"
            );
        }

        if (faceImage == null || faceImage.isBlank()) {
            throw new RuntimeException(
                    "Face descriptor obligatoire"
            );
        }

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(email.trim())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        List<Double> descriptor =
                parseDescriptor(faceImage);

        validateDescriptor(descriptor);

        /*
         * On stocke le descriptor sous forme JSON.
         */
        try {

            utilisateur.setFaceDescriptor(
                    objectMapper.writeValueAsString(
                            descriptor
                    )
            );

            utilisateurRepository.save(utilisateur);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Impossible d'enregistrer le visage",
                    e
            );
        }
    }

    // =====================================================
    // LOGIN FACE
    // =====================================================

    public AuthenticationResponse loginWithFace(
            String email,
            String faceImage) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException(
                    "Email obligatoire"
            );
        }

        if (faceImage == null || faceImage.isBlank()) {
            throw new RuntimeException(
                    "Face descriptor obligatoire"
            );
        }

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(email.trim())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        if (Boolean.FALSE.equals(
                utilisateur.getActif()
        )) {

            throw new RuntimeException(
                    "Ce compte est désactivé"
            );
        }

        if (utilisateur.getFaceDescriptor() == null ||
                utilisateur.getFaceDescriptor().isBlank()) {

            throw new RuntimeException(
                    "Aucun visage enregistré pour ce compte"
            );
        }

        List<Double> storedDescriptor =
                parseDescriptor(
                        utilisateur.getFaceDescriptor()
                );

        List<Double> receivedDescriptor =
                parseDescriptor(faceImage);

        validateDescriptor(storedDescriptor);
        validateDescriptor(receivedDescriptor);

        double distance =
                euclideanDistance(
                        storedDescriptor,
                        receivedDescriptor
                );

        System.out.println(
                "Face distance = " + distance
        );

        if (distance > FACE_THRESHOLD) {

            throw new RuntimeException(
                    "Visage non reconnu"
            );
        }

        // =================================================
        // JWT
        // =================================================

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

        String accessToken =
                jwtService.generateToken(
                        userDetails
                );

        RefreshToken refreshToken =
                refreshTokenService
                        .createRefreshToken(
                                utilisateur
                        );

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
    }

    // =====================================================
    // PARSE DESCRIPTOR
    // =====================================================

    private List<Double> parseDescriptor(
            String faceData) {

        try {

            return objectMapper.readValue(
                    faceData,
                    new TypeReference<List<Double>>() {}
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Face descriptor invalide"
            );
        }
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private void validateDescriptor(
            List<Double> descriptor) {

        if (descriptor == null ||
                descriptor.size() != 128) {

            throw new RuntimeException(
                    "Face descriptor invalide : " +
                    "128 valeurs attendues"
            );
        }
    }

    // =====================================================
    // EUCLIDEAN DISTANCE
    // =====================================================

    private double euclideanDistance(
            List<Double> first,
            List<Double> second) {

        double sum = 0.0;

        for (int i = 0; i < first.size(); i++) {

            double difference =
                    first.get(i) - second.get(i);

            sum += difference * difference;
        }

        return Math.sqrt(sum);
    }
}