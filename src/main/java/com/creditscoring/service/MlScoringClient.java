package com.creditscoring.service;

import com.creditscoring.entity.Client;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MlScoringClient {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String ML_URL =
            "http://localhost:8000/predict";

    public Map<String, Object> predict(
            DemandeCredit demande
    ) {

        if (demande == null) {
            throw new RuntimeException("Demande introuvable");
        }

        if (demande.getClient() == null) {
            throw new RuntimeException(
                    "Client associé à la demande introuvable"
            );
        }

        Client client = demande.getClient();
        Utilisateur utilisateur = client;

        Map<String, Object> payload = new HashMap<>();

        payload.put(
                "duration_months",
                demande.getDuree()
        );

        payload.put(
                "credit_amount",
                demande.getMontant()
        );

        payload.put(
                "purpose",
                normaliser(
                        demande.getTypeCredit(),
                        "radio/tv"
                )
        );

        payload.put(
                "employment_years",
                convertirAncienneteEmploi(
                        client.getAncienneteEmploi()
                )
        );

        payload.put(
                "personal_status",
                normaliser(
                        client.getSituationFamiliale(),
                        "male single"
                )
        );

        payload.put(
                "age",
                calculerAge(
                        client.getDateNaissance()
                )
        );

        payload.put(
                "job",
                normaliser(
                        client.getProfession(),
                        "skilled"
                )
        );

        payload.put(
                "dependents",
                client.getNombrePersonnesACharge() != null
                        ? client.getNombrePersonnesACharge()
                        : 0
        );

        payload.put(
                "telephone",
                utilisateur.getTelephone() != null
                        && !utilisateur.getTelephone().isBlank()
                        ? "yes"
                        : "none"
        );

        ResponseEntity<Map> response =
                restTemplate.postForEntity(
                        ML_URL,
                        payload,
                        Map.class
                );

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null) {

            throw new RuntimeException(
                    "Le service ML n'a pas répondu correctement"
            );
        }

        return response.getBody();
    }

    private int calculerAge(LocalDate dateNaissance) {

        if (dateNaissance == null) {
            return 30;
        }

        return Period.between(
                dateNaissance,
                LocalDate.now()
        ).getYears();
    }

    private String convertirAncienneteEmploi(
            Integer anciennete
    ) {

        if (anciennete == null) {
            return "1<=X<4";
        }

        if (anciennete < 1) {
            return "<1";
        }

        if (anciennete < 4) {
            return "1<=X<4";
        }

        if (anciennete < 7) {
            return "4<=X<7";
        }

        return ">=7";
    }

    private String normaliser(
            String value,
            String defaultValue
    ) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim().toLowerCase();
    }
}
