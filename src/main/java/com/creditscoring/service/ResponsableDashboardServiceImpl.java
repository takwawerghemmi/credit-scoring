package com.creditscoring.service;

import com.creditscoring.dto.reponse.ResponsableDashboardResponse;
import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.entity.ValidationDemande;
import com.creditscoring.enums.NiveauValidation;
import com.creditscoring.repository.CreditScoreRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.repository.ValidationDemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ResponsableDashboardServiceImpl
        implements ResponsableDashboardService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final ValidationDemandeRepository validationDemandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    // =========================================================
    // DASHBOARD RESPONSABLE CONNECTÉ
    // =========================================================

    @Override
    public ResponsableDashboardResponse getDashboard(String email) {

        // =====================================================
        // Récupérer le Responsable connecté
        // =====================================================

        Utilisateur responsable =
                utilisateurRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Responsable connecté introuvable : " + email
                                )
                        );

        // =====================================================
        // Récupérer uniquement les demandes affectées
        // à ce Responsable
        // =====================================================

        List<DemandeCredit> demandes =
                demandeCreditRepository.findByResponsableId(
                        responsable.getId()
                );

        // =====================================================
        // Scores
        // =====================================================

        List<CreditScore> scores =
                creditScoreRepository.findAll();

        // =====================================================
        // Validations
        // =====================================================

        List<ValidationDemande> validations =
                validationDemandeRepository.findAll();

        Map<Long, CreditScore> latestScoreByDemande =
                getLatestScores(scores);

        Map<Long, Set<NiveauValidation>> validationsByDemande =
                getValidationsByDemande(validations);

        // =====================================================
        // Construire les dossiers
        // =====================================================

        List<ResponsableDashboardResponse.DossierResponsable> dossiers =
                demandes.stream()
                        .map(demande ->
                                buildDossier(
                                        demande,
                                        latestScoreByDemande,
                                        validationsByDemande
                                )
                        )
                        .toList();

        // =====================================================
        // Dossiers prêts pour la validation Responsable
        // =====================================================

        List<ResponsableDashboardResponse.DossierResponsable>
                dossiersAControler =
                dossiers.stream()
                        .filter(
                                ResponsableDashboardServiceImpl::isReadyForResponsable
                        )
                        .sorted(
                                Comparator.comparing(
                                        ResponsableDashboardResponse.DossierResponsable::getDateDemande,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .toList();

        // =====================================================
        // Derniers dossiers
        // =====================================================

        List<ResponsableDashboardResponse.DossierResponsable>
                derniersDossiers =
                dossiers.stream()
                        .sorted(
                                Comparator.comparing(
                                        ResponsableDashboardResponse.DossierResponsable::getDateDemande,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )
                        .limit(10)
                        .toList();

        // =====================================================
        // KPI
        // =====================================================

        ResponsableDashboardResponse.Kpi kpi =
                buildKpi(
                        dossiers,
                        dossiersAControler,
                        scores,
                        validations
                );

        return ResponsableDashboardResponse.builder()
                .kpi(kpi)
                .dossiersAControler(dossiersAControler)
                .derniersDossiers(derniersDossiers)
                .build();
    }

    // =========================================================
    // DÉTAIL D'UNE DEMANDE
    // =========================================================

    @Override
    public ResponsableDashboardResponse.DossierResponsable getDossier(
            Long demandeId,
            String email
    ) {

        // =====================================================
        // Responsable connecté
        // =====================================================

        Utilisateur responsable =
                utilisateurRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Responsable connecté introuvable : " + email
                                )
                        );

        // =====================================================
        // Demande
        // =====================================================

        DemandeCredit demande =
                demandeCreditRepository.findById(demandeId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Demande introuvable : " + demandeId
                                )
                        );

        // =====================================================
        // Vérifier que cette demande appartient
        // au Responsable connecté
        // =====================================================

        if (demande.getResponsable() == null
                || !demande.getResponsable()
                .getId()
                .equals(responsable.getId())) {

            throw new RuntimeException(
                    "Accès refusé : cette demande n'est pas affectée à ce Responsable."
            );
        }

        // =====================================================
        // Scores
        // =====================================================

        List<CreditScore> scores =
                creditScoreRepository.findAll();

        // =====================================================
        // Validations
        // =====================================================

        List<ValidationDemande> validations =
                validationDemandeRepository.findAll();

        Map<Long, CreditScore> latestScoreByDemande =
                getLatestScores(scores);

        Map<Long, Set<NiveauValidation>> validationsByDemande =
                getValidationsByDemande(validations);

        return buildDossier(
                demande,
                latestScoreByDemande,
                validationsByDemande
        );
    }

    // =========================================================
    // KPI
    // =========================================================

    private ResponsableDashboardResponse.Kpi buildKpi(
            List<ResponsableDashboardResponse.DossierResponsable> dossiers,
            List<ResponsableDashboardResponse.DossierResponsable> dossiersAControler,
            List<CreditScore> scores,
            List<ValidationDemande> validations
    ) {

        long premieresValidations =
                validations.stream()
                        .filter(v ->
                                v.getNiveau() == NiveauValidation.CONSEILLER
                        )
                        .filter(v ->
                                Boolean.TRUE.equals(v.getDecision())
                        )
                        .filter(v ->
                                v.getDemandeCredit() != null
                                        && dossiers.stream().anyMatch(
                                        d -> d.getDemandeId()
                                                .equals(
                                                        v.getDemandeCredit().getId()
                                                )
                                )
                        )
                        .count();

        long deuxiemesValidations =
                validations.stream()
                        .filter(v ->
                                v.getNiveau() == NiveauValidation.RESPONSABLE
                        )
                        .filter(v ->
                                Boolean.TRUE.equals(v.getDecision())
                        )
                        .filter(v ->
                                v.getDemandeCredit() != null
                                        && dossiers.stream().anyMatch(
                                        d -> d.getDemandeId()
                                                .equals(
                                                        v.getDemandeCredit().getId()
                                                )
                                )
                        )
                        .count();

        long approuves =
                dossiers.stream()
                        .filter(d ->
                                "APPROUVEE".equals(d.getStatut())
                        )
                        .count();

        long refuses =
                dossiers.stream()
                        .filter(d ->
                                "REFUSEE".equals(d.getStatut())
                        )
                        .count();

        long faible =
                dossiers.stream()
                        .filter(d ->
                                isRisk(d, "FAIBLE")
                                        || isRisk(d, "TRES_FAIBLE")
                        )
                        .count();

        long moyen =
                dossiers.stream()
                        .filter(d ->
                                isRisk(d, "MOYEN")
                        )
                        .count();

        long eleve =
                dossiers.stream()
                        .filter(d ->
                                isRisk(d, "ELEVE")
                                        || isRisk(d, "TRES_ELEVE")
                        )
                        .count();

        double scoreMoyen =
                scores.stream()
                        .filter(s ->
                                s.getDemandeCredit() != null
                                        && dossiers.stream().anyMatch(
                                        d -> d.getDemandeId()
                                                .equals(
                                                        s.getDemandeCredit().getId()
                                                )
                                )
                        )
                        .map(CreditScore::getScore)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

        long anomalies =
                dossiers.stream()
                        .filter(d ->
                                "APPROUVEE".equals(d.getStatut())
                                        && !d.isDeuxiemeValidation()
                        )
                        .count();

        return ResponsableDashboardResponse.Kpi.builder()
                .totalDemandes(dossiers.size())
                .dossiersAControler(dossiersAControler.size())
                .premieresValidations(premieresValidations)
                .deuxiemesValidations(deuxiemesValidations)
                .dossiersApprouves(approuves)
                .dossiersRefuses(refuses)
                .risqueFaible(faible)
                .risqueMoyen(moyen)
                .risqueEleve(eleve)
                .anomalies(anomalies)
                .scoreMoyen(
                        Math.round(scoreMoyen * 100.0) / 100.0
                )
                .build();
    }

    // =========================================================
    // CONSTRUIRE DOSSIER
    // =========================================================

    private ResponsableDashboardResponse.DossierResponsable buildDossier(
            DemandeCredit demande,
            Map<Long, CreditScore> scores,
            Map<Long, Set<NiveauValidation>> validations
    ) {

        CreditScore score =
                scores.get(demande.getId());

        Set<NiveauValidation> niveaux =
                validations.getOrDefault(
                        demande.getId(),
                        Collections.emptySet()
                );

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

        boolean premiere =
                niveaux.contains(NiveauValidation.CONSEILLER);

        boolean deuxieme =
                niveaux.contains(NiveauValidation.RESPONSABLE);

        boolean pret =
                premiere && !deuxieme;

        return ResponsableDashboardResponse.DossierResponsable.builder()
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
                .score(
                        score != null
                                ? score.getScore()
                                : null
                )
                .niveauRisque(
                        score != null
                                && score.getNiveauRisque() != null
                                ? score.getNiveauRisque().name()
                                : null
                )
                .premiereValidation(premiere)
                .deuxiemeValidation(deuxieme)
                .pretPourValidation(pret)
                .dateDemande(demande.getDateDemande())
                .build();
    }

    // =========================================================
    // DERNIER SCORE
    // =========================================================

    private Map<Long, CreditScore> getLatestScores(
            List<CreditScore> scores
    ) {

        return scores.stream()
                .filter(s ->
                        s.getDemandeCredit() != null
                )
                .collect(
                        Collectors.toMap(
                                s ->
                                        s.getDemandeCredit().getId(),
                                s -> s,
                                (s1, s2) -> {

                                    if (s1.getDateCalcul() == null) {
                                        return s2;
                                    }

                                    if (s2.getDateCalcul() == null) {
                                        return s1;
                                    }

                                    return s1.getDateCalcul()
                                            .isAfter(s2.getDateCalcul())
                                            ? s1
                                            : s2;
                                }
                        )
                );
    }

    // =========================================================
    // VALIDATIONS PAR DEMANDE
    // =========================================================

    private Map<Long, Set<NiveauValidation>>
    getValidationsByDemande(
            List<ValidationDemande> validations
    ) {

        Map<Long, Set<NiveauValidation>> result =
                new HashMap<>();

        for (ValidationDemande validation : validations) {

            if (validation.getDemandeCredit() == null) {
                continue;
            }

            if (!Boolean.TRUE.equals(
                    validation.getDecision()
            )) {
                continue;
            }

            result.computeIfAbsent(
                            validation.getDemandeCredit().getId(),
                            k -> EnumSet.noneOf(NiveauValidation.class)
                    )
                    .add(validation.getNiveau());
        }

        return result;
    }

    // =========================================================
    // PRÊT POUR RESPONSABLE
    // =========================================================

    private static boolean isReadyForResponsable(
            ResponsableDashboardResponse.DossierResponsable dossier
    ) {

        return dossier.isPremiereValidation()
                && !dossier.isDeuxiemeValidation();
    }

    // =========================================================
    // RISQUE
    // =========================================================

    private static boolean isRisk(
            ResponsableDashboardResponse.DossierResponsable dossier,
            String risk
    ) {

        return risk.equals(dossier.getNiveauRisque());
    }
}