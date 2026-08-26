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

    @Override
    @Transactional
    public DecisionResponse prendreDecision(
            DecisionRequest request,
            String role,
            String emailDirecteur
    ) {

        if (!"ROLE_DIRECTEUR".equals(role)) {

            throw new RuntimeException(
                    "Accès interdit : seul le directeur peut prendre la décision finale."
            );
        }

        Utilisateur directeur =
                utilisateurRepository.findByEmail(
                        emailDirecteur
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Directeur connecté introuvable."
                        )
                );

        CreditScore score =
                creditScoreRepository.findById(
                        request.getCreditScoreId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Score introuvable."
                        )
                );

        if (score.getDemandeCredit() == null) {

            throw new RuntimeException(
                    "La demande associée au score est introuvable."
            );
        }

        DemandeCredit demande =
                score.getDemandeCredit();

        if (demande.getStatut()
                != StatutDemande.EN_ATTENTE) {

            throw new RuntimeException(
                    "La demande doit être en attente de décision."
            );
        }

        ValidationDemande validationResponsable =
                validationDemandeRepository
                        .findByDemandeCreditIdAndNiveau(
                                demande.getId(),
                                NiveauValidation.RESPONSABLE
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "La validation du responsable est obligatoire avant la décision finale."
                                )
                        );

        if (!Boolean.TRUE.equals(
                validationResponsable.getDecision()
        )) {

            throw new RuntimeException(
                    "La validation du responsable n'est pas favorable."
            );
        }

        if (decisionRepository
                .findByCreditScore(score)
                .isPresent()) {

            throw new RuntimeException(
                    "Une décision existe déjà pour cette demande."
            );
        }

        DecisionCredit decision =
                new DecisionCredit();

        decision.setCreditScore(score);
        decision.setAccepte(
                request.getAccepte()
        );
        decision.setCommentaire(
                request.getCommentaire()
        );

        // =====================================================
        // APPROUVEE
        // =====================================================

        if (Boolean.TRUE.equals(
                request.getAccepte()
        )) {

            demande.setStatut(
                    StatutDemande.APPROUVEE
            );

            ContratService contratServiceLocal =
                    this.contratService;

            contratServiceLocal.creerContratAutomatiquement(
                    demande,
                    directeur
            );

            // Client: décision + contrat
            if (demande.getClient() != null) {

                notificationService
                        .creerNotificationAutomatique(
                                demande.getClient(),
                                demande,
                                "Crédit approuvé",
                                "Votre demande de crédit #"
                                        + demande.getId()
                                        + " a été approuvée par le Directeur. Le contrat a été créé automatiquement."
                        );
            }

        } else {

            // =====================================================
            // REFUSEE
            // =====================================================

            demande.setStatut(
                    StatutDemande.REFUSEE
            );

            demande.setMotifRefus(
                    request.getCommentaire()
            );

            if (demande.getClient() != null) {

                notificationService
                        .creerNotificationAutomatique(
                                demande.getClient(),
                                demande,
                                "Crédit refusé",
                                "Votre demande de crédit #"
                                        + demande.getId()
                                        + " a été refusée par le Directeur. Motif : "
                                        + request.getCommentaire()
                        );
            }
        }

        decisionRepository.save(
                decision
        );

        creditScoreRepository.save(
                score
        );

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