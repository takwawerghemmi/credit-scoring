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

        // =====================================================
        // BROUILLON
        // =====================================================

        TRANSITIONS_VALIDES.put(
                StatutDemande.BROUILLON,
                Set.of(
                        StatutDemande.SOUMISE
                )
        );

        // =====================================================
        // SOUMISE
        // =====================================================

        TRANSITIONS_VALIDES.put(
                StatutDemande.SOUMISE,
                Set.of(
                        StatutDemande.EN_ANALYSE,
                        StatutDemande.DOCUMENTS_MANQUANTS
                )
        );

        // =====================================================
        // EN_ANALYSE
        // =====================================================

        TRANSITIONS_VALIDES.put(
                StatutDemande.EN_ANALYSE,
                Set.of(
                        StatutDemande.DOCUMENTS_MANQUANTS,
                        StatutDemande.EN_ATTENTE
                )
        );

        // =====================================================
        // EN_ATTENTE
        // =====================================================
        // Le CONSEILLER peut commencer l'analyse.
        // Le RESPONSABLE_CREDIT peut prendre la décision finale.

        TRANSITIONS_VALIDES.put(
                StatutDemande.EN_ATTENTE,
                Set.of(
                        StatutDemande.EN_ANALYSE,
                        StatutDemande.APPROUVEE,
                        StatutDemande.REFUSEE
                )
        );

        // =====================================================
        // DOCUMENTS_MANQUANTS
        // =====================================================

        TRANSITIONS_VALIDES.put(
                StatutDemande.DOCUMENTS_MANQUANTS,
                Set.of(
                        StatutDemande.SOUMISE
                )
        );

        // =====================================================
        // APPROUVEE
        // =====================================================

        TRANSITIONS_VALIDES.put(
                StatutDemande.APPROUVEE,
                Set.of(
                        StatutDemande.CONTRAT_SIGNE
                )
        );

        // =====================================================
        // CONTRAT_SIGNE
        // =====================================================

        TRANSITIONS_VALIDES.put(
                StatutDemande.CONTRAT_SIGNE,
                Set.of(
                        StatutDemande.CLOTUREE
                )
        );
    }

    // ========================================================
    // TRANSITION
    // ========================================================

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

        // =====================================================
        // 1. TRANSITION VALIDE
        // =====================================================

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

        // =====================================================
        // 2. UTILISATEUR CONNECTÉ
        // =====================================================

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

        // =====================================================
        // 3. AUTORISATIONS + AFFECTATION
        // =====================================================

        verifierAutorisationTransition(
                demande,
                utilisateur,
                statutActuel,
                nouveauStatut,
                roleUtilisateur
        );

        // =====================================================
        // 4. ANCIEN STATUT
        // =====================================================

        String ancienneValeur =
                statutActuel != null
                        ? statutActuel.name()
                        : null;

        // =====================================================
        // 5. NOUVEAU STATUT
        // =====================================================

        demande.setStatut(
                nouveauStatut
        );

        demande.setCommentaireAnalyse(
                request.getCommentaire()
        );

        // =====================================================
        // 6. DATE ANALYSE
        // =====================================================

        if (nouveauStatut ==
                StatutDemande.EN_ANALYSE) {

            demande.setDateAnalyse(
                    LocalDateTime.now()
            );
        }

        // =====================================================
        // 7. DATE DECISION
        // =====================================================

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

        // =====================================================
        // 8. SAUVEGARDE
        // =====================================================

        demandeRepository.save(
                demande
        );

        // =====================================================
        // 9. HISTORIQUE
        // =====================================================

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
    // AUTORISATIONS
    // ========================================================

    private void verifierAutorisationTransition(
            DemandeCredit demande,
            Utilisateur utilisateur,
            StatutDemande statutActuel,
            StatutDemande nouveauStatut,
            String roleUtilisateur
    ) {

        // =====================================================
        // CONSEILLER
        // EN_ATTENTE -> EN_ANALYSE
        // =====================================================

        if (statutActuel ==
                StatutDemande.EN_ATTENTE
                && nouveauStatut ==
                StatutDemande.EN_ANALYSE) {

            if (!"ROLE_CONSEILLER".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le CONSEILLER peut commencer l'analyse."
                );
            }

            verifierConseillerAffecte(
                    demande,
                    utilisateur
            );

            return;
        }

        // =====================================================
        // CONSEILLER
        // SOUMISE -> EN_ANALYSE
        // =====================================================

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

            verifierConseillerAffecte(
                    demande,
                    utilisateur
            );

            return;
        }

        // =====================================================
        // CONSEILLER
        // EN_ANALYSE -> DOCUMENTS_MANQUANTS
        // =====================================================

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

            verifierConseillerAffecte(
                    demande,
                    utilisateur
            );

            return;
        }

        // =====================================================
        // CONSEILLER
        // EN_ANALYSE -> EN_ATTENTE
        // =====================================================

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

            verifierConseillerAffecte(
                    demande,
                    utilisateur
            );

            return;
        }

        // =====================================================
        // RESPONSABLE CREDIT
        // EN_ATTENTE -> APPROUVEE
        // EN_ATTENTE -> REFUSEE
        // =====================================================

        if (statutActuel ==
                StatutDemande.EN_ATTENTE
                && (
                nouveauStatut ==
                        StatutDemande.APPROUVEE
                        || nouveauStatut ==
                        StatutDemande.REFUSEE
        )) {

            if (!"ROLE_RESPONSABLE_CREDIT".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le RESPONSABLE CREDIT peut prendre la décision finale."
                );
            }

            verifierResponsableAffecte(
                    demande,
                    utilisateur
            );

            return;
        }

        // =====================================================
        // RESPONSABLE CREDIT
        // APPROUVEE -> CONTRAT_SIGNE
        // =====================================================

        if (statutActuel ==
                StatutDemande.APPROUVEE
                && nouveauStatut ==
                StatutDemande.CONTRAT_SIGNE) {

            if (!"ROLE_RESPONSABLE_CREDIT".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le RESPONSABLE CREDIT peut faire avancer la demande vers le contrat."
                );
            }

            verifierResponsableAffecte(
                    demande,
                    utilisateur
            );

            return;
        }

        // =====================================================
        // RESPONSABLE CREDIT
        // CONTRAT_SIGNE -> CLOTUREE
        // =====================================================

        if (statutActuel ==
                StatutDemande.CONTRAT_SIGNE
                && nouveauStatut ==
                StatutDemande.CLOTUREE) {

            if (!"ROLE_RESPONSABLE_CREDIT".equals(
                    roleUtilisateur
            )) {

                throw new RuntimeException(
                        "Seul le RESPONSABLE CREDIT peut clôturer la demande."
                );
            }

            verifierResponsableAffecte(
                    demande,
                    utilisateur
            );
        }
    }

    // ========================================================
    // VÉRIFIER CONSEILLER AFFECTÉ
    // ========================================================

    private void verifierConseillerAffecte(
            DemandeCredit demande,
            Utilisateur utilisateur
    ) {

        if (demande.getConseiller() == null) {

            throw new RuntimeException(
                    "Cette demande n'est affectée à aucun Conseiller."
            );
        }

        if (!Objects.equals(
                demande.getConseiller().getId(),
                utilisateur.getId()
        )) {

            throw new RuntimeException(
                    "Accès interdit : cette demande est affectée à un autre Conseiller."
            );
        }
    }

    // ========================================================
    // VÉRIFIER RESPONSABLE AFFECTÉ
    // ========================================================

    private void verifierResponsableAffecte(
            DemandeCredit demande,
            Utilisateur utilisateur
    ) {

        if (demande.getResponsable() == null) {

            throw new RuntimeException(
                    "Cette demande n'est affectée à aucun Responsable Crédit."
            );
        }

        if (!Objects.equals(
                demande.getResponsable().getId(),
                utilisateur.getId()
        )) {

            throw new RuntimeException(
                    "Accès interdit : cette demande est affectée à un autre Responsable Crédit."
            );
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

        etapes.add(
                buildEtape(
                        "Contrat signé",
                        statut == StatutDemande.CONTRAT_SIGNE
                                || statut == StatutDemande.CLOTUREE,
                        null
                )
        );

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
                .etape(nom)
                .statut(
                        complete
                                ? "COMPLETE"
                                : "EN_ATTENTE"
                )
                .date(date)
                .complete(complete)
                .build();
    }
}