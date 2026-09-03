package com.creditscoring.service;

import com.creditscoring.dto.reponse.DecisionResponse;
import com.creditscoring.dto.request.DecisionRequest;
import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DecisionCredit;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.entity.ValidationDemande;
import com.creditscoring.enums.NiveauValidation;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.repository.CreditScoreRepository;
import com.creditscoring.repository.DecisionCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.repository.ValidationDemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecisionCreditServiceImpl
        implements DecisionCreditService {

    private final CreditScoreRepository creditScoreRepository;
    private final DecisionCreditRepository decisionRepository;
    private final ValidationDemandeRepository validationDemandeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ContratService contratService;
    private final NotificationService notificationService;

    // =========================================================
    // DÉCISION FINALE - RESPONSABLE CRÉDIT
    // =========================================================

    @Override
    @Transactional
    public DecisionResponse prendreDecision(
            DecisionRequest request,
            String role,
            String emailResponsable
    ) {

        // =====================================================
        // Vérifier le rôle
        // =====================================================

        if (!"ROLE_RESPONSABLE_CREDIT".equals(role)) {

            throw new RuntimeException(
                    "Accès interdit : seul le Responsable Crédit peut prendre la décision finale."
            );
        }

        // =====================================================
        // Récupérer le Responsable connecté
        // =====================================================

        Utilisateur responsable =
                utilisateurRepository.findByEmail(
                        emailResponsable
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Responsable Crédit connecté introuvable."
                        )
                );

        // =====================================================
        // Récupérer le score
        // =====================================================

        CreditScore score =
                creditScoreRepository.findById(
                        request.getCreditScoreId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Score introuvable."
                        )
                );

        // =====================================================
        // Vérifier la demande
        // =====================================================

        if (score.getDemandeCredit() == null) {

            throw new RuntimeException(
                    "La demande associée au score est introuvable."
            );
        }

        DemandeCredit demande =
                score.getDemandeCredit();

        // =====================================================
        // Vérifier que le Responsable est bien affecté
        // à cette demande
        // =====================================================

        if (demande.getResponsable() == null
                || !demande.getResponsable()
                .getId()
                .equals(responsable.getId())) {

            throw new RuntimeException(
                    "Accès interdit : cette demande n'est pas affectée à ce Responsable."
            );
        }

        // =====================================================
        // Vérifier le statut
        // =====================================================

        if (demande.getStatut()
                != StatutDemande.EN_ATTENTE) {

            throw new RuntimeException(
                    "La demande doit être en attente de décision."
            );
        }

        // =====================================================
        // Vérifier la validation Responsable
        // =====================================================

        ValidationDemande validationResponsable =
                validationDemandeRepository
                        .findByDemandeCreditIdAndNiveau(
                                demande.getId(),
                                NiveauValidation.RESPONSABLE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "La validation du Responsable est obligatoire avant la décision finale."
                                )
                        );

        // =====================================================
        // Vérifier que la validation est favorable
        // =====================================================

        if (!Boolean.TRUE.equals(
                validationResponsable.getDecision()
        )) {

            throw new RuntimeException(
                    "La validation du Responsable n'est pas favorable."
            );
        }

        // =====================================================
        // Vérifier qu'une décision n'existe pas déjà
        // =====================================================

        if (decisionRepository
                .findByCreditScore(score)
                .isPresent()) {

            throw new RuntimeException(
                    "Une décision existe déjà pour cette demande."
            );
        }

        // =====================================================
        // Créer la décision
        // =====================================================

        DecisionCredit decision =
                new DecisionCredit();

        decision.setCreditScore(
                score
        );

        decision.setAccepte(
                request.getAccepte()
        );

        decision.setCommentaire(
                request.getCommentaire()
        );

        // =====================================================
        // RESPONSABLE APPROUVE
        // =====================================================

        if (Boolean.TRUE.equals(
                request.getAccepte()
        )) {

            demande.setStatut(
                    StatutDemande.APPROUVEE
            );

            // =================================================
            // Créer automatiquement le contrat
            // avec le Responsable comme décideur
            // =================================================

            contratService.creerContratAutomatiquement(
                    demande,
                    responsable
            );

            // =================================================
            // Notification Client
            // =================================================

            if (demande.getClient() != null) {

                notificationService
                        .creerNotificationAutomatique(
                                demande.getClient(),
                                demande,
                                "Crédit approuvé",
                                "Votre demande de crédit #"
                                        + demande.getId()
                                        + " a été approuvée par le Responsable Crédit. "
                                        + "Le contrat a été créé automatiquement."
                        );
            }

        } else {

            // =================================================
            // RESPONSABLE REFUSE
            // =================================================

            demande.setStatut(
                    StatutDemande.REFUSEE
            );

            demande.setMotifRefus(
                    request.getCommentaire()
            );

            // =================================================
            // Notification Client
            // =================================================

            if (demande.getClient() != null) {

                notificationService
                        .creerNotificationAutomatique(
                                demande.getClient(),
                                demande,
                                "Crédit refusé",
                                "Votre demande de crédit #"
                                        + demande.getId()
                                        + " a été refusée par le Responsable Crédit. "
                                        + "Motif : "
                                        + request.getCommentaire()
                        );
            }
        }

        // =====================================================
        // Sauvegarder la décision
        // =====================================================

        decisionRepository.save(
                decision
        );

        creditScoreRepository.save(
                score
        );

        // =====================================================
        // Préparer la réponse
        // =====================================================

        DecisionResponse response =
                new DecisionResponse();

        response.setAccepte(
                decision.getAccepte()
        );

        response.setCommentaire(
                decision.getCommentaire()
        );

        return response;
    }
}