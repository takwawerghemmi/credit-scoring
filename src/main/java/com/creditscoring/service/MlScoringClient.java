package com.creditscoring.service;

import com.creditscoring.dto.reponse.ExplicationFacteurResponse;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MlScoringClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ml.url:http://localhost:8000}")
    private String mlBaseUrl;

    public Map<String, Object> predict(DemandeCredit demande) {

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

        try {

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            mlBaseUrl + "/predict",
                            payload,
                            Map.class
                    );

            if (!response.getStatusCode().is2xxSuccessful()
                    || response.getBody() == null) {

                log.error(
                        "Réponse invalide du service ML"
                );

                return fallbackResult(demande);
            }

            return response.getBody();

        } catch (RestClientException e) {

            log.error(
                    "Service ML indisponible : {}",
                    e.getMessage()
            );

            return fallbackResult(demande);
        }
    }

    public ExplicationFacteurResponse getExplication(
            DemandeCredit demande
    ) {

        Map<String, Object> result = predict(demande);

        double probabiliteDefaut =
                toDouble(
                        result.get("probability_default")
                );

        double creditScore =
                toDouble(
                        result.get("credit_score")
                );

        String niveauRisque =
                result.get("risk_level") != null
                        ? String.valueOf(
                        result.get("risk_level")
                )
                        : "INCONNU";

        List<ExplicationFacteurResponse.FacteurResponse> facteurs =
                new ArrayList<>();

        Object rawFactors =
                result.get("factors");

        if (rawFactors instanceof List<?> factorList) {

            for (Object item : factorList) {

                if (item instanceof Map<?, ?> factorMap) {

                    ExplicationFacteurResponse.FacteurResponse facteur =
                            ExplicationFacteurResponse.FacteurResponse
                                    .builder()
                                    .feature(
                                            factorMap.get("feature") != null
                                                    ? String.valueOf(
                                                    factorMap.get("feature")
                                            )
                                                    : null
                                    )
                                    .impact(
                                            factorMap.get("impact") != null
                                                    ? String.valueOf(
                                                    factorMap.get("impact")
                                            )
                                                    : null
                                    )
                                    .contribution(
                                            toDoubleOrNull(
                                                    factorMap.get(
                                                            "contribution"
                                                    )
                                            )
                                    )
                                    .build();

                    facteurs.add(facteur);
                }
            }
        }

        return ExplicationFacteurResponse.builder()
                .demandeCreditId(
                        demande.getId()
                )
                .creditScore(
                        creditScore
                )
                .niveauRisque(
                        niveauRisque
                )
                .probabiliteDefaut(
                        probabiliteDefaut
                )
                .facteurs(
                        facteurs
                )
                .build();
    }

    private Map<String, Object> fallbackResult(
            DemandeCredit demande
    ) {

        log.warn(
                "Fallback ML activé pour la demande {}",
                demande.getId()
        );

        Map<String, Object> fallback =
                new HashMap<>();

        fallback.put(
                "prediction",
                -1
        );

        fallback.put(
                "probability_default",
                -1.0
        );

        fallback.put(
                "risk_level",
                "INCONNU"
        );

        fallback.put(
                "credit_score",
                -1
        );

        fallback.put(
                "factors",
                List.of()
        );

        fallback.put(
                "fallback",
                true
        );

        return fallback;
    }

    private int calculerAge(
            LocalDate dateNaissance
    ) {

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

    private double toDouble(
            Object value
    ) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(
                    value.toString()
            );
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double toDoubleOrNull(
            Object value
    ) {

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(
                    value.toString()
            );
        } catch (Exception e) {
            return null;
        }
    }
}