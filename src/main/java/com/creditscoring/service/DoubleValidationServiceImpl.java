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

        verifierRole(
                role,
                "ROLE_CONSEILLER"
        );

        DemandeCredit demande =
                trouverDemande(demandeId);

        Utilisateur validateur =
                trouverValidateur(emailValidateur);

        verifierPasDejaValidee(
                demandeId,
                NiveauValidation.CONSEILLER
        );

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

        demande.setStatut(
                StatutDemande.EN_ATTENTE
        );

        demandeCreditRepository.save(
                demande
        );

        // =====================================================
        // NOTIFIER LES RESPONSABLES
        // =====================================================

        notifierResponsables(
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

        DemandeCredit demande =
                trouverDemande(demandeId);

        Utilisateur validateur =
                trouverValidateur(
                        emailValidateur
                );

        verifierPremiereValidation(
                demandeId
        );

        verifierPasDejaValidee(
                demandeId,
                NiveauValidation.RESPONSABLE
        );

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
        // RESPONSABLE ACCEPTE
        // =====================================================

        if (Boolean.TRUE.equals(
                request.getDecision()
        )) {

            demande.setStatut(
                    StatutDemande.EN_ATTENTE
            );

            demandeCreditRepository.save(
                    demande
            );

            // Notifier les directeurs
            notifierDirecteurs(
                    demande,
                    "Demande prête pour décision finale",
                    "La demande de crédit #"
                            + demandeId
                            + " a été validée par le Responsable et attend la décision du Directeur."
            );

            // Notifier le client
            notifierClient(
                    demande,
                    "Demande validée",
                    "Votre demande de crédit #"
                            + demandeId
                            + " a été validée par le Responsable et est prête pour la décision finale."
            );

            return "Validation Responsable acceptée. La demande est approuvée et prête pour la décision finale.";
        }

        // =====================================================
        // RESPONSABLE REFUSE
        // =====================================================

        demande.setStatut(
                StatutDemande.REFUSEE
        );

        demandeCreditRepository.save(
                demande
        );

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

    private Utilisateur trouverValidateur(
            String email
    ) {

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Validateur connecté introuvable"
                        )
                );
    }

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
    // NOTIFIER RESPONSABLES
    // =========================================================

    private void notifierResponsables(
            DemandeCredit demande,
            String titre,
            String message
    ) {

        utilisateurRepository.findAll()
                .stream()
                .filter(
                        u ->
                                u.getRole() != null
                                        && u.getRole().getNom() != null
                                        && u.getRole()
                                        .getNom()
                                        .equalsIgnoreCase(
                                                "RESPONSABLE_CREDIT"
                                        )
                )
                .forEach(
                        responsable ->
                                notificationService
                                        .creerNotificationAutomatique(
                                                responsable,
                                                demande,
                                                titre,
                                                message
                                        )
                );
    }

    // =========================================================
    // NOTIFIER DIRECTEURS
    // =========================================================

    private void notifierDirecteurs(
            DemandeCredit demande,
            String titre,
            String message
    ) {

        utilisateurRepository.findAll()
                .stream()
                .filter(
                        u ->
                                u.getRole() != null
                                        && u.getRole().getNom() != null
                                        && u.getRole()
                                        .getNom()
                                        .equalsIgnoreCase(
                                                "DIRECTEUR"
                                        )
                )
                .forEach(
                        directeur ->
                                notificationService
                                        .creerNotificationAutomatique(
                                                directeur,
                                                demande,
                                                titre,
                                                message
                                        )
                );
    }
}