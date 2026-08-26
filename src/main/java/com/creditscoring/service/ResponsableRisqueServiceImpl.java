package com.creditscoring.service;

import com.creditscoring.dto.reponse.ResponsableRisqueResponse;
import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.CreditScoreRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ResponsableRisqueServiceImpl
        implements ResponsableRisqueService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final CreditScoreRepository creditScoreRepository;

    @Override
    public List<ResponsableRisqueResponse> getDossiersARisque() {

        List<DemandeCredit> demandes =
                demandeCreditRepository.findAll();

        Map<Long, CreditScore> scores =
                getLatestScores(
                        creditScoreRepository.findAll()
                );

        return demandes.stream()
                .map(demande ->
                        buildResponse(
                                demande,
                                scores.get(demande.getId()),
                                null
                        )
                )
                .filter(this::isDossierARisque)
                .sorted(
                        Comparator
                                .comparing(
                                        this::riskWeight
                                )
                                .reversed()
                                .thenComparing(
                                        ResponsableRisqueResponse::getScore,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                )
                .toList();
    }

    @Override
    public List<ResponsableRisqueResponse> getDossiersPrioritaires() {

        List<DemandeCredit> demandes =
                demandeCreditRepository.findAll();

        Map<Long, CreditScore> scores =
                getLatestScores(
                        creditScoreRepository.findAll()
                );

        List<ResponsableRisqueResponse> result =
                demandes.stream()
                        .map(demande ->
                                buildResponse(
                                        demande,
                                        scores.get(demande.getId()),
                                        null
                                )
                        )
                        .sorted(
                                Comparator
                                        .comparingInt(
                                                this::priorityWeight
                                        )
                                        .reversed()
                                        .thenComparing(
                                                ResponsableRisqueResponse::getScore,
                                                Comparator.nullsLast(
                                                        Comparator.naturalOrder()
                                                )
                                        )
                                        .thenComparing(
                                                ResponsableRisqueResponse::getMontant,
                                                Comparator.nullsLast(
                                                        Comparator.reverseOrder()
                                                )
                                        )
                                        .thenComparing(
                                                ResponsableRisqueResponse::getDateDemande,
                                                Comparator.nullsLast(
                                                        Comparator.naturalOrder()
                                                )
                                        )
                        )
                        .toList();

        // Ajouter un rang
        List<ResponsableRisqueResponse> ranked =
                new ArrayList<>();

        for (int i = 0; i < result.size(); i++) {

            ResponsableRisqueResponse dossier =
                    result.get(i);

            dossier.setRangPriorite(i + 1);

            ranked.add(dossier);
        }

        return ranked;
    }

    private Map<Long, CreditScore> getLatestScores(
            List<CreditScore> scores
    ) {

        return scores.stream()
                .filter(
                        score ->
                                score.getDemandeCredit() != null
                )
                .collect(
                        Collectors.toMap(
                                score ->
                                        score.getDemandeCredit().getId(),
                                Function.identity(),
                                (s1, s2) -> {

                                    if (s1.getDateCalcul() == null) {
                                        return s2;
                                    }

                                    if (s2.getDateCalcul() == null) {
                                        return s1;
                                    }

                                    return s1.getDateCalcul()
                                            .isAfter(
                                                    s2.getDateCalcul()
                                            )
                                            ? s1
                                            : s2;
                                }
                        )
                );
    }

    private ResponsableRisqueResponse buildResponse(
            DemandeCredit demande,
            CreditScore score,
            Integer rang
    ) {

        String nom = "";
        String prenom = "";

        if (demande.getClient() != null) {

            if (demande.getClient().getNom() != null) {
                nom = demande.getClient().getNom();
            }

            if (demande.getClient().getPrenom() != null) {
                prenom = demande.getClient().getPrenom();
            }
        }

        String niveauRisque =
                score != null
                        && score.getNiveauRisque() != null
                        ? score.getNiveauRisque().name()
                        : null;

        Double scoreValue =
                score != null
                        ? score.getScore()
                        : null;

        return ResponsableRisqueResponse.builder()
                .demandeId(demande.getId())
                .clientNom(nom)
                .clientPrenom(prenom)
                .montant(demande.getMontant())
                .duree(demande.getDuree())
                .typeCredit(demande.getTypeCredit())
                .statut(
                        demande.getStatut() != null
                                ? demande.getStatut().name()
                                : null
                )
                .score(scoreValue)
                .niveauRisque(niveauRisque)
                .priorite(calculerPriorite(niveauRisque, scoreValue))
                .rangPriorite(rang)
                .dateDemande(demande.getDateDemande())
                .build();
    }

    private boolean isDossierARisque(
            ResponsableRisqueResponse dossier
    ) {

        if (dossier.getNiveauRisque() == null) {
            return false;
        }

        String risque =
                dossier.getNiveauRisque().toUpperCase();

        return risque.equals("MOYEN")
                || risque.equals("ELEVE")
                || risque.equals("TRES_ELEVE");
    }

    private int riskWeight(
            ResponsableRisqueResponse dossier
    ) {

        String risque =
                dossier.getNiveauRisque();

        if (risque == null) {
            return 0;
        }

        return switch (risque.toUpperCase()) {
            case "TRES_ELEVE" -> 4;
            case "ELEVE" -> 3;
            case "MOYEN" -> 2;
            case "FAIBLE" -> 1;
            case "TRES_FAIBLE" -> 0;
            default -> 0;
        };
    }

    private int priorityWeight(
            ResponsableRisqueResponse dossier
    ) {

        int weight = 0;

        String risque =
                dossier.getNiveauRisque();

        if (risque != null) {

            weight += switch (
                    risque.toUpperCase()
                    ) {
                case "TRES_ELEVE" -> 100;
                case "ELEVE" -> 80;
                case "MOYEN" -> 50;
                case "FAIBLE" -> 20;
                default -> 0;
            };
        }

        Double score =
                dossier.getScore();

        if (score != null) {

            if (score < 500) {
                weight += 40;
            } else if (score < 600) {
                weight += 30;
            } else if (score < 700) {
                weight += 20;
            } else if (score < 800) {
                weight += 10;
            }
        }

        Double montant =
                dossier.getMontant();

        if (montant != null) {

            if (montant >= 50000) {
                weight += 30;
            } else if (montant >= 20000) {
                weight += 20;
            } else if (montant >= 10000) {
                weight += 10;
            }
        }

        return weight;
    }

    private String calculerPriorite(
            String niveauRisque,
            Double score
    ) {

        int weight = 0;

        if (niveauRisque != null) {

            weight += switch (
                    niveauRisque.toUpperCase()
                    ) {
                case "TRES_ELEVE" -> 100;
                case "ELEVE" -> 80;
                case "MOYEN" -> 50;
                case "FAIBLE" -> 20;
                default -> 0;
            };
        }

        if (score != null) {

            if (score < 500) {
                weight += 40;
            } else if (score < 600) {
                weight += 30;
            } else if (score < 700) {
                weight += 20;
            } else if (score < 800) {
                weight += 10;
            }
        }

        if (weight >= 120) {
            return "CRITIQUE";
        }

        if (weight >= 90) {
            return "HAUTE";
        }

        if (weight >= 50) {
            return "MOYENNE";
        }

        return "FAIBLE";
    }
}
