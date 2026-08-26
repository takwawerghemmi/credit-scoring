package com.creditscoring.service;

import com.creditscoring.dto.request.WorkflowTransitionRequest;
import com.creditscoring.dto.reponse.SuiviDemandeResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Historique;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.HistoriqueRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

    private final DemandeCreditRepository demandeRepository;
    private final HistoriqueRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;

    private static final Map<
            StatutDemande,
            Set<StatutDemande>
            > TRANSITIONS_VALIDES = new HashMap<>();

    static {

        // ==============================
        // BROUILLON
        // ==============================

        TRANSITIONS_VALIDES.put(
                StatutDemande.BROUILLON,
                Set.of(
                        StatutDemande.SOUMISE
                )
        );

        // ==============================
        // SOUMISE
        // ==============================

        TRANSITIONS_VALIDES.put(
                StatutDemande.SOUMISE,
                Set.of(
                        StatutDemande.EN_ANALYSE,
                        StatutDemande.DOCUMENTS_MANQUANTS
                )
        );

        // ==============================
        // EN_ANALYSE
        // ==============================

        TRANSITIONS_VALIDES.put(
                StatutDemande.EN_ANALYSE,
                Set.of(
                        StatutDemande.DOCUMENTS_MANQUANTS,
                        StatutDemande.EN_ATTENTE
                )
        );

        // ==============================
        // DOCUMENTS_MANQUANTS
        // ==============================

        TRANSITIONS_VALIDES.put(
                StatutDemande.DOCUMENTS_MANQUANTS,
                Set.of(
                        StatutDemande.SOUMISE
                )
        );

        // ==============================
        // EN_ATTENTE
        // ==============================
        // Seul DIRECTEUR peut faire:
        // EN_ATTENTE -> APPROUVEE
        // EN_ATTENTE -> REFUSEE

        TRANSITIONS_VALIDES.put(
                StatutDemande.EN_ATTENTE,
                Set.of(
                        StatutDemande.APPROUVEE,
                        StatutDemande.REFUSEE
                )
        );

        // ==============================
        // APPROUVEE
        // ==============================

        TRANSITIONS_VALIDES.put(
                StatutDemande.APPROUVEE,
                Set.of(
                        StatutDemande.CONTRAT_SIGNE
                )
        );

        // ==============================
        // CONTRAT_SIGNE
        // ==============================

        TRANSITIONS_VALIDES.put(
                StatutDemande.CONTRAT_SIGNE,
                Set.of(
                        StatutDemande.CLOTUREE
                )
        );
    }

    @Override
    @Transactional
    public void transitionner(
            Long demandeId,
            WorkflowTransitionRequest request,
            String emailUtilisateur,
            String roleUtilisateur
    ) {

        DemandeCredit demande =
                demandeRepository.findById(demandeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        StatutDemande statutActuel =
                demande.getStatut();

        StatutDemande nouveauStatut =
                request.getNouveauStatut();

        Set<StatutDemande> transitionsPermises =
                TRANSITIONS_VALIDES.getOrDefault(
                        statutActuel,
                        Collections.emptySet()
                );

        // ============================================
        // 1. Vérifier que la transition existe
        // ============================================

        if (!transitionsPermises.contains(
                nouveauStatut
        )) {

            throw new RuntimeException(
                    "Transition invalide de "
                            + statutActuel
                            + " vers "
                            + nouveauStatut
            );
        }

        // ============================================
        // 2. Vérifier l'utilisateur connecté
        // ============================================

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(
                                emailUtilisateur
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable"
                                )
                        );

        // ============================================
        // 3. Règles métier par rôle
        // ============================================

        verifierAutorisationTransition(
                statutActuel,
                nouveauStatut,
                roleUtilisateur
        );

        // ============================================
        // 4. Ancien statut
        // ============================================

        String ancienneValeur =
                statutActuel != null
                        ? statutActuel.name()
                        : null;

        // ============================================
        // 5. Nouveau statut
        // ============================================

        demande.setStatut(
                nouveauStatut
        );

        demande.setCommentaireAnalyse(
                request.getCommentaire()
        );

        // ============================================
        // 6. Date analyse
        // ============================================

        if (nouveauStatut ==
                StatutDemande.EN_ANALYSE) {

            demande.setDateAnalyse(
                    LocalDateTime.now()
            );
        }

        // ============================================
        // 7. Date décision
        // ============================================

        if (nouveauStatut ==
                StatutDemande.APPROUVEE
                || nouveauStatut ==
                StatutDemande.REFUSEE) {

            demande.setDateDecision(
                    LocalDateTime.now()
            );

            if (nouveauStatut ==
                    StatutDemande.REFUSEE) {

                demande.setMotifRefus(
                        request.getCommentaire()
                );
            }
        }

        // ============================================
        // 8. Sauvegarder demande
        // ============================================

        demandeRepository.save(
                demande
        );

        // ============================================
        // 9. Historique
        // ============================================

        Historique historique =
                Historique.builder()
                        .action(
                                "CHANGEMENT_STATUT"
                        )
                        .description(
                                "Changement du statut de la demande #"
                                        + demandeId
                                        + " de "
                                        + ancienneValeur
                                        + " vers "
                                        + nouveauStatut.name()
                        )
                        .entite(
                                "DemandeCredit"
                        )
                        .entiteId(
                                demandeId
                        )
                        .ancienneValeur(
                                ancienneValeur
                        )
                        .nouvelleValeur(
                                nouveauStatut.name()
                        )
                        .utilisateur(
                                utilisateur
                        )
                        .build();

        historiqueRepository.save(
                historique
        );
    }

    // ========================================================
    // REGLES D'AUTORISATION
    // ========================================================

    private void verifierAutorisationTransition(
            StatutDemande statutActuel,
            StatutDemande nouveauStatut,
            String roleUtilisateur
    ) {

        // ============================================
        // EN_ATTENTE -> APPROUVEE / REFUSEE
        // SEUL DIRECTEUR
        // ============================================

        if (statutActuel ==
                StatutDemande.EN_ATTENTE
                && (
                nouveauStatut ==
                        StatutDemande.APPROUVEE
                        || nouveauStatut ==
                        StatutDemande.REFUSEE
        )) {

            if (!"ROLE_DIRECTEUR".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le DIRECTEUR peut prendre la décision finale."
                );
            }
        }

        // ============================================
        // SOUMISE -> EN_ANALYSE
        // CONSEILLER
        // ============================================

        if (statutActuel ==
                StatutDemande.SOUMISE
                && nouveauStatut ==
                StatutDemande.EN_ANALYSE) {

            if (!"ROLE_CONSEILLER".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le CONSEILLER peut commencer l'analyse."
                );
            }
        }

        // ============================================
        // EN_ANALYSE -> DOCUMENTS_MANQUANTS
        // CONSEILLER
        // ============================================

        if (statutActuel ==
                StatutDemande.EN_ANALYSE
                && nouveauStatut ==
                StatutDemande.DOCUMENTS_MANQUANTS) {

            if (!"ROLE_CONSEILLER".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le CONSEILLER peut demander des documents manquants."
                );
            }
        }

        // ============================================
        // EN_ANALYSE -> EN_ATTENTE
        // CONSEILLER
        // ============================================

        if (statutActuel ==
                StatutDemande.EN_ANALYSE
                && nouveauStatut ==
                StatutDemande.EN_ATTENTE) {

            if (!"ROLE_CONSEILLER".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le CONSEILLER peut terminer l'analyse."
                );
            }
        }

        // ============================================
        // APPROUVEE -> CONTRAT_SIGNE
        // DIRECTEUR
        // ============================================

        if (statutActuel ==
                StatutDemande.APPROUVEE
                && nouveauStatut ==
                StatutDemande.CONTRAT_SIGNE) {

            if (!"ROLE_DIRECTEUR".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le DIRECTEUR peut faire avancer la demande vers le contrat."
                );
            }
        }

        // ============================================
        // CONTRAT_SIGNE -> CLOTUREE
        // DIRECTEUR
        // ============================================

        if (statutActuel ==
                StatutDemande.CONTRAT_SIGNE
                && nouveauStatut ==
                StatutDemande.CLOTUREE) {

            if (!"ROLE_DIRECTEUR".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le DIRECTEUR peut clôturer la demande."
                );
            }
        }
    }

    // ========================================================
    // GET STATUT
    // ========================================================

    @Override
    public StatutDemande getStatutActuel(
            Long demandeId
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        demandeId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        return demande.getStatut();
    }

    // ========================================================
    // GET SUIVI
    // ========================================================

    @Override
    public SuiviDemandeResponse getSuivi(
            Long demandeId
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        demandeId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        StatutDemande statut =
                demande.getStatut();

        List<SuiviDemandeResponse.EtapeSuivi> etapes =
                new ArrayList<>();

        // ============================================
        // SOUMISE
        // ============================================

        etapes.add(
                buildEtape(
                        "Soumise",
                        statut == StatutDemande.SOUMISE
                                || statut == StatutDemande.EN_ANALYSE
                                || statut == StatutDemande.DOCUMENTS_MANQUANTS
                                || statut == StatutDemande.EN_ATTENTE
                                || statut == StatutDemande.APPROUVEE
                                || statut == StatutDemande.REFUSEE
                                || statut == StatutDemande.CONTRAT_SIGNE
                                || statut == StatutDemande.CLOTUREE,
                        demande.getDateDemande() != null
                                ? demande.getDateDemande()
                                .atStartOfDay()
                                : null
                )
        );

        // ============================================
        // ANALYSE
        // ============================================

        etapes.add(
                buildEtape(
                        "Analyse",
                        statut == StatutDemande.EN_ANALYSE
                                || statut == StatutDemande.EN_ATTENTE
                                || statut == StatutDemande.APPROUVEE
                                || statut == StatutDemande.REFUSEE
                                || statut == StatutDemande.CONTRAT_SIGNE
                                || statut == StatutDemande.CLOTUREE,
                        demande.getDateAnalyse()
                )
        );

        // ============================================
        // SCORE
        // ============================================

        etapes.add(
                buildEtape(
                        "Score calculé",
                        statut == StatutDemande.EN_ATTENTE
                                || statut == StatutDemande.APPROUVEE
                                || statut == StatutDemande.REFUSEE
                                || statut == StatutDemande.CONTRAT_SIGNE
                                || statut == StatutDemande.CLOTUREE,
                        null
                )
        );

        // ============================================
        // DECISION
        // ============================================

        etapes.add(
                buildEtape(
                        "Décision",
                        statut == StatutDemande.APPROUVEE
                                || statut == StatutDemande.REFUSEE
                                || statut == StatutDemande.CONTRAT_SIGNE
                                || statut == StatutDemande.CLOTUREE,
                        demande.getDateDecision()
                )
        );

        // ============================================
        // CONTRAT
        // ============================================

        etapes.add(
                buildEtape(
                        "Contrat signé",
                        statut == StatutDemande.CONTRAT_SIGNE
                                || statut == StatutDemande.CLOTUREE,
                        null
                )
        );

        // ============================================
        // CLOTURE
        // ============================================

        etapes.add(
                buildEtape(
                        "Clôturée",
                        statut == StatutDemande.CLOTUREE,
                        null
                )
        );

        return SuiviDemandeResponse.builder()
                .demandeId(
                        demandeId
                )
                .statutActuel(
                        statut.name()
                )
                .etapes(
                        etapes
                )
                .build();
    }

    private SuiviDemandeResponse.EtapeSuivi buildEtape(
            String nom,
            boolean complete,
            LocalDateTime date
    ) {

        return SuiviDemandeResponse.EtapeSuivi.builder()
                .etape(
                        nom
                )
                .statut(
                        complete
                                ? "COMPLETE"
                                : "EN_ATTENTE"
                )
                .date(
                        date
                )
                .complete(
                        complete
                )
                .build();
    }
}