package com.creditscoring.service;

import com.creditscoring.dto.reponse.CreditScoreResponse;
import com.creditscoring.dto.request.CreditScoreRequest;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.ResponsableCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.NiveauRisque;
import com.creditscoring.repository.CreditScoreRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
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
    private final UtilisateurRepository utilisateurRepository;
    private final MlScoringClient mlScoringClient;

    // =========================================================
    // CALCUL DU SCORE
    // =========================================================

    @Override
    public CreditScoreResponse calculerScore(
            CreditScoreRequest request,
            String emailUtilisateur
    ) {

        if (request == null
                || request.getDemandeCreditId() == null) {

            throw new RuntimeException(
                    "L'identifiant de la demande est obligatoire."
            );
        }

        DemandeCredit demande =
                demandeCreditRepository
                        .findById(request.getDemandeCreditId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable."
                                )
                        );

        verifierAccesDemande(
                demande,
                utilisateur
        );

        if (demande.getClient() == null) {

            throw new RuntimeException(
                    "Client associé à la demande introuvable"
            );
        }

        // =====================================================
        // Appel ML
        // =====================================================

        Map<String, Object> mlResult =
                mlScoringClient.predict(demande);

        double score =
                toDouble(
                        mlResult.get("credit_score")
                );

        double probabilityDefault =
                toDouble(
                        mlResult.get("probability_default")
                );

        String riskLevel =
                String.valueOf(
                        mlResult.get("risk_level")
                );

        String decision =
                calculerDecision(
                        probabilityDefault
                );

        NiveauRisque niveauRisque =
                convertirNiveauRisque(
                        riskLevel
                );

        // =====================================================
        // IMPORTANT :
        // UPDATE si le score existe déjà
        // INSERT sinon
        // =====================================================

        CreditScore entity =
                creditScoreRepository
                        .findByDemandeCreditId(
                                demande.getId()
                        )
                        .orElseGet(
                                CreditScore::new
                        );

        entity.setScore(score);

        entity.setNiveauRisque(
                niveauRisque
        );

        entity.setDateCalcul(
                LocalDateTime.now()
        );

        entity.setScoreConfiance(
                Math.max(
                        0.0,
                        Math.min(
                                1.0,
                                1.0 - probabilityDefault
                        )
                )
        );

        entity.setDetailsScore(
                "Modèle ML CatBoost - "
                        + "probabilité de défaut : "
                        + probabilityDefault
        );

        entity.setRecommandation(
                "Décision calculée par le modèle ML : "
                        + decision
        );

        entity.setDemandeCredit(
                demande
        );

        creditScoreRepository.save(
                entity
        );

        return convertirResponse(
                entity,
                decision
        );
    }

    // =========================================================
    // TOUS LES SCORES ACCESSIBLES
    // =========================================================

    @Override
    public List<CreditScoreResponse> getAllScores(
            String emailUtilisateur
    ) {

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable."
                                )
                        );

        List<CreditScore> scores;

        if (utilisateur instanceof Conseiller conseiller) {

            scores =
                    creditScoreRepository
                            .findByDemandeCreditConseillerId(
                                    conseiller.getId()
                            );

        } else if (utilisateur instanceof ResponsableCredit responsable) {

            scores =
                    creditScoreRepository
                            .findByDemandeCreditResponsableId(
                                    responsable.getId()
                            );

        } else if (utilisateur instanceof Client client) {

            scores =
                    creditScoreRepository
                            .findByDemandeCreditClientId(
                                    client.getId()
                            );

        } else {

            throw new RuntimeException(
                    "Accès interdit."
            );
        }

        return scores
                .stream()
                .map(score -> {

                    String decision =
                            extraireDecision(score);

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
            Long id,
            String emailUtilisateur
    ) {

        CreditScore score =
                creditScoreRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Score introuvable"
                                )
                        );

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable."
                                )
                        );

        verifierAccesDemande(
                score.getDemandeCredit(),
                utilisateur
        );

        String decision =
                extraireDecision(score);

        return convertirResponse(
                score,
                decision
        );
    }

    // =========================================================
    // VÉRIFICATION ACCÈS
    // =========================================================

    private void verifierAccesDemande(
            DemandeCredit demande,
            Utilisateur utilisateur
    ) {

        if (demande == null) {

            throw new RuntimeException(
                    "Demande introuvable."
            );
        }

        // -----------------------------------------------------
        // CLIENT
        // -----------------------------------------------------

        if (utilisateur instanceof Client client) {

            if (demande.getClient() == null
                    || !demande.getClient()
                    .getId()
                    .equals(client.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "n'appartient pas au Client connecté."
                );
            }

            return;
        }

        // -----------------------------------------------------
        // CONSEILLER
        // -----------------------------------------------------

        if (utilisateur instanceof Conseiller conseiller) {

            if (demande.getConseiller() == null
                    || !demande.getConseiller()
                    .getId()
                    .equals(conseiller.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "est affectée à un autre Conseiller."
                );
            }

            return;
        }

        // -----------------------------------------------------
        // RESPONSABLE
        // -----------------------------------------------------

        if (utilisateur instanceof ResponsableCredit responsable) {

            if (demande.getResponsable() == null
                    || !demande.getResponsable()
                    .getId()
                    .equals(responsable.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "est affectée à un autre Responsable."
                );
            }

            return;
        }

        throw new RuntimeException(
                "Accès interdit."
        );
    }

    // =========================================================
    // ENTITY -> RESPONSE
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
    // EXTRAIRE DECISION
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
                            recommandation.lastIndexOf(":") + 1
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
    // DÉCISION
    // =========================================================

    private String calculerDecision(
            double probabilityDefault
    ) {

        return probabilityDefault < 0.50
                ? "ACCEPTE"
                : "REFUSE";
    }

    // =========================================================
    // NIVEAU RISQUE
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
    // DOUBLE
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