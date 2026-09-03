package com.creditscoring.service;

import com.creditscoring.dto.request.ResponsableValidationRequest;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.entity.ValidationDemande;
import com.creditscoring.enums.NiveauValidation;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.repository.ValidationDemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoubleValidationServiceImpl
        implements DoubleValidationService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ValidationDemandeRepository validationDemandeRepository;
    private final NotificationService notificationService;

    // =========================================================
    // PREMIÈRE VALIDATION - CONSEILLER
    // =========================================================

    @Override
    @Transactional
    public String premiereValidation(
            Long demandeId,
            String emailValidateur,
            String role
    ) {

        // Vérifier le rôle
        verifierRole(
                role,
                "ROLE_CONSEILLER"
        );

        // Récupérer la demande
        DemandeCredit demande =
                trouverDemande(demandeId);

        // Récupérer le Conseiller connecté
        Utilisateur validateur =
                trouverValidateur(emailValidateur);

        // =====================================================
        // Vérifier que la demande est bien en analyse
        // =====================================================

        if (demande.getStatut() != StatutDemande.EN_ANALYSE) {

            throw new RuntimeException(
                    "La demande doit être en analyse avant la première validation."
            );
        }

        // =====================================================
        // Vérifier que le Conseiller connecté est affecté
        // à cette demande
        // =====================================================

        if (demande.getConseiller() == null
                || !demande.getConseiller()
                .getId()
                .equals(validateur.getId())) {

            throw new RuntimeException(
                    "Ce Conseiller n'est pas affecté à cette demande."
            );
        }

        // =====================================================
        // Vérifier qu'il n'y a pas déjà une validation
        // =====================================================

        verifierPasDejaValidee(
                demandeId,
                NiveauValidation.CONSEILLER
        );

        // =====================================================
        // Créer la validation
        // =====================================================

        ValidationDemande validation =
                ValidationDemande.builder()
                        .demandeCredit(demande)
                        .validateur(validateur)
                        .niveau(NiveauValidation.CONSEILLER)
                        .decision(true)
                        .commentaire(
                                "Première validation effectuée."
                        )
                        .build();

        validationDemandeRepository.save(
                validation
        );

        // =====================================================
        // Passer la demande en attente du Responsable
        // =====================================================

        demande.setStatut(
                StatutDemande.EN_ATTENTE
        );

        demandeCreditRepository.save(
                demande
        );

        // =====================================================
        // Notifier UNIQUEMENT le Responsable affecté
        // =====================================================

        notifierResponsable(
                demande,
                "Nouvelle validation à traiter",
                "La demande de crédit #"
                        + demandeId
                        + " a été validée par le Conseiller et attend votre validation."
        );

        return "Première validation effectuée avec succès.";
    }

    // =========================================================
    // VALIDATION RESPONSABLE
    // =========================================================

    @Override
    @Transactional
    public String validationResponsable(
            Long demandeId,
            ResponsableValidationRequest request,
            String emailValidateur
    ) {

        if (request == null) {

            throw new RuntimeException(
                    "La demande de validation est obligatoire."
            );
        }

        if (request.getDecision() == null) {

            throw new RuntimeException(
                    "La décision est obligatoire."
            );
        }

        if (request.getCommentaire() == null
                || request.getCommentaire()
                .trim()
                .isEmpty()) {

            throw new RuntimeException(
                    "Le commentaire est obligatoire."
            );
        }

        // =====================================================
        // Récupérer la demande
        // =====================================================

        DemandeCredit demande =
                trouverDemande(demandeId);

        // =====================================================
        // Récupérer le Responsable connecté
        // =====================================================

        Utilisateur validateur =
                trouverValidateur(emailValidateur);

        // =====================================================
        // Vérifier que le Responsable connecté est bien
        // affecté à cette demande
        // =====================================================

        if (demande.getResponsable() == null
                || !demande.getResponsable()
                .getId()
                .equals(validateur.getId())) {

            throw new RuntimeException(
                    "Ce Responsable n'est pas affecté à cette demande."
            );
        }

        // =====================================================
        // Vérifier que la demande est en attente de validation
        // =====================================================

        if (demande.getStatut() != StatutDemande.EN_ATTENTE) {

            throw new RuntimeException(
                    "La demande doit être en attente avant la validation du Responsable."
            );
        }

        // =====================================================
        // Vérifier première validation Conseiller
        // =====================================================

        verifierPremiereValidation(
                demandeId
        );

        // =====================================================
        // Vérifier double validation
        // =====================================================

        verifierPasDejaValidee(
                demandeId,
                NiveauValidation.RESPONSABLE
        );

        // =====================================================
        // Créer la validation Responsable
        // =====================================================

        ValidationDemande validation =
                ValidationDemande.builder()
                        .demandeCredit(demande)
                        .validateur(validateur)
                        .niveau(
                                NiveauValidation.RESPONSABLE
                        )
                        .decision(
                                request.getDecision()
                        )
                        .commentaire(
                                request
                                        .getCommentaire()
                                        .trim()
                        )
                        .build();

        validationDemandeRepository.save(
                validation
        );

        // =====================================================
        // RESPONSABLE ACCEPTE LA VALIDATION
        // =====================================================

        if (Boolean.TRUE.equals(
                request.getDecision()
        )) {

            // On garde la demande en attente de décision finale.
            demande.setStatut(
                    StatutDemande.EN_ATTENTE
            );

            demandeCreditRepository.save(
                    demande
            );

            // Notification au client
            notifierClient(
                    demande,
                    "Validation Responsable",
                    "Votre demande de crédit #"
                            + demandeId
                            + " a été validée par le Responsable et est prête pour la décision finale."
            );

            return "Validation Responsable acceptée. La demande est prête pour la décision finale du Responsable.";
        }

        // =====================================================
        // RESPONSABLE REFUSE
        // =====================================================

        demande.setStatut(
                StatutDemande.REFUSEE
        );

        demande.setMotifRefus(
                request.getCommentaire().trim()
        );

        demandeCreditRepository.save(
                demande
        );

        // Notification au client
        notifierClient(
                demande,
                "Demande refusée",
                "Votre demande de crédit #"
                        + demandeId
                        + " a été refusée par le Responsable. Motif : "
                        + request.getCommentaire().trim()
        );

        return "Validation Responsable refusée. La demande est refusée.";
    }

    // =========================================================
    // TROUVER DEMANDE
    // =========================================================

    private DemandeCredit trouverDemande(
            Long id
    ) {

        return demandeCreditRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );
    }

    // =========================================================
    // TROUVER VALIDATEUR
    // =========================================================

    private Utilisateur trouverValidateur(
            String email
    ) {

        return utilisateurRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Validateur connecté introuvable"
                        )
                );
    }

    // =========================================================
    // VÉRIFIER LE RÔLE
    // =========================================================

    private void verifierRole(
            String roleActuel,
            String roleAttendu
    ) {

        if (!roleAttendu.equals(roleActuel)) {

            throw new RuntimeException(
                    "Accès interdit pour ce niveau de validation"
            );
        }
    }

    // =========================================================
    // VÉRIFIER PREMIÈRE VALIDATION
    // =========================================================

    private void verifierPremiereValidation(
            Long demandeId
    ) {

        List<ValidationDemande> validations =
                validationDemandeRepository
                        .findByDemandeCreditId(
                                demandeId
                        );

        boolean premiereValidee =
                validations.stream()
                        .anyMatch(
                                v ->
                                        v.getNiveau()
                                                == NiveauValidation.CONSEILLER
                                                && Boolean.TRUE.equals(
                                                v.getDecision()
                                        )
                        );

        if (!premiereValidee) {

            throw new RuntimeException(
                    "La première validation du Conseiller est obligatoire avant la validation du Responsable."
            );
        }
    }

    // =========================================================
    // VÉRIFIER DOUBLE VALIDATION
    // =========================================================

    private void verifierPasDejaValidee(
            Long demandeId,
            NiveauValidation niveau
    ) {

        List<ValidationDemande> validations =
                validationDemandeRepository
                        .findByDemandeCreditId(
                                demandeId
                        );

        boolean existe =
                validations.stream()
                        .anyMatch(
                                v ->
                                        v.getNiveau()
                                                == niveau
                        );

        if (existe) {

            throw new RuntimeException(
                    "Cette validation a déjà été effectuée."
            );
        }
    }

    // =========================================================
    // NOTIFIER CLIENT
    // =========================================================

    private void notifierClient(
            DemandeCredit demande,
            String titre,
            String message
    ) {

        if (demande.getClient() != null) {

            notificationService
                    .creerNotificationAutomatique(
                            demande.getClient(),
                            demande,
                            titre,
                            message
                    );
        }
    }

    // =========================================================
    // NOTIFIER RESPONSABLE AFFECTÉ UNIQUEMENT
    // =========================================================

    private void notifierResponsable(
            DemandeCredit demande,
            String titre,
            String message
    ) {

        if (demande.getResponsable() != null) {

            notificationService
                    .creerNotificationAutomatique(
                            demande.getResponsable(),
                            demande,
                            titre,
                            message
                    );
        }
    }
}