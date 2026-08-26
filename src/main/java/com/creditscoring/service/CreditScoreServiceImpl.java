package com.creditscoring.service;

import com.creditscoring.dto.reponse.CreditScoreResponse;
import com.creditscoring.dto.request.CreditScoreRequest;
import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.NiveauRisque;
import com.creditscoring.repository.CreditScoreRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreditScoreServiceImpl
        implements CreditScoreService {

    private final CreditScoreRepository creditScoreRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final MlScoringClient mlScoringClient;

    // =========================================================
    // CALCUL DU SCORE
    // =========================================================

    @Override
    public CreditScoreResponse calculerScore(
            CreditScoreRequest request
    ) {

        if (request == null
                || request.getDemandeCreditId() == null) {

            throw new RuntimeException(
                    "L'identifiant de la demande est obligatoire."
            );
        }

        DemandeCredit demande =
                demandeCreditRepository
                        .findById(
                                request.getDemandeCreditId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        if (demande.getClient() == null) {

            throw new RuntimeException(
                    "Client associé à la demande introuvable"
            );
        }

        // =====================================================
        // Appel au modèle ML
        // =====================================================

        Map<String, Object> mlResult =
                mlScoringClient.predict(
                        demande
                );

        double score =
                toDouble(
                        mlResult.get(
                                "credit_score"
                        )
                );

        double probabilityDefault =
                toDouble(
                        mlResult.get(
                                "probability_default"
                        )
                );

        String riskLevel =
                String.valueOf(
                        mlResult.get(
                                "risk_level"
                        )
                );

        // =====================================================
        // Décision UNIQUE basée sur la probabilité de défaut
        // =====================================================

        String decision =
                calculerDecision(
                        probabilityDefault
                );

        NiveauRisque niveauRisque =
                convertirNiveauRisque(
                        riskLevel
                );

        // =====================================================
        // Persistance
        // =====================================================

        CreditScore entity =
                CreditScore.builder()
                        .score(score)
                        .niveauRisque(niveauRisque)
                        .dateCalcul(LocalDateTime.now())
                        .scoreConfiance(
                                Math.max(
                                        0.0,
                                        Math.min(
                                                1.0,
                                                1.0
                                                        - probabilityDefault
                                        )
                                )
                        )
                        .detailsScore(
                                "Modèle ML CatBoost - "
                                        + "probabilité de défaut : "
                                        + probabilityDefault
                        )
                        .recommandation(
                                "Décision calculée par le modèle ML : "
                                        + decision
                        )
                        .demandeCredit(demande)
                        .build();

        creditScoreRepository.save(
                entity
        );

        return convertirResponse(
                entity,
                decision
        );
    }

    // =========================================================
    // TOUS LES SCORES
    // =========================================================

    @Override
    public List<CreditScoreResponse> getAllScores() {

        return creditScoreRepository
                .findAll()
                .stream()
                .map(score -> {

                    String decision =
                            extraireDecision(
                                    score
                            );

                    return convertirResponse(
                            score,
                            decision
                    );
                })
                .toList();
    }

    // =========================================================
    // SCORE PAR ID
    // =========================================================

    @Override
    public CreditScoreResponse getScore(
            Long id
    ) {

        CreditScore score =
                creditScoreRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Score introuvable"
                                )
                        );

        String decision =
                extraireDecision(
                        score
                );

        return convertirResponse(
                score,
                decision
        );
    }

    // =========================================================
    // CONVERSION ENTITY -> RESPONSE
    // =========================================================

    private CreditScoreResponse convertirResponse(
            CreditScore score,
            String decision
    ) {

        CreditScoreResponse response =
                new CreditScoreResponse();

        response.setId(
                score.getId()
        );

        response.setScore(
                score.getScore()
        );

        response.setNiveauRisque(
                score.getNiveauRisque()
        );

        response.setDecision(
                decision
        );

        return response;
    }

    // =========================================================
    // EXTRAIRE LA DÉCISION STOCKÉE
    // =========================================================

    private String extraireDecision(
            CreditScore score
    ) {

        String recommandation =
                score.getRecommandation();

        if (recommandation != null
                && recommandation.contains(":")) {

            String decision =
                    recommandation.substring(
                            recommandation
                                    .lastIndexOf(":")
                                    + 1
                    ).trim();

            if ("ACCEPTE".equalsIgnoreCase(
                    decision
            )) {
                return "ACCEPTE";
            }

            if ("REFUSE".equalsIgnoreCase(
                    decision
            )) {
                return "REFUSE";
            }
        }

        // -----------------------------------------------------
        // Fallback pour anciens scores
        // -----------------------------------------------------

        if (score.getNiveauRisque() ==
                NiveauRisque.TRES_FAIBLE
                || score.getNiveauRisque() ==
                NiveauRisque.FAIBLE) {

            return "ACCEPTE";
        }

        if (score.getNiveauRisque() ==
                NiveauRisque.TRES_ELEVE
                || score.getNiveauRisque() ==
                NiveauRisque.ELEVE) {

            return "REFUSE";
        }

        return "A_REVOIR";
    }

    // =========================================================
    // DÉCISION ML
    // =========================================================

    private String calculerDecision(
            double probabilityDefault
    ) {

        return probabilityDefault < 0.50
                ? "ACCEPTE"
                : "REFUSE";
    }

    // =========================================================
    // CONVERSION NIVEAU RISQUE
    // =========================================================

    private NiveauRisque convertirNiveauRisque(
            String riskLevel
    ) {

        if (riskLevel == null
                || riskLevel.isBlank()) {

            return NiveauRisque.MOYEN;
        }

        try {

            return NiveauRisque.valueOf(
                    riskLevel
                            .trim()
                            .toUpperCase()
                            .replace(" ", "_")
                            .replace("-", "_")
            );

        } catch (Exception e) {

            return NiveauRisque.MOYEN;
        }
    }

    // =========================================================
    // CONVERSION DOUBLE
    // =========================================================

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

            throw new RuntimeException(
                    "Valeur numérique ML invalide : "
                            + value
            );
        }
    }
}