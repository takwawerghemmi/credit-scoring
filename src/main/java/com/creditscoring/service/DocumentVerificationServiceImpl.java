package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentVerificationResponse;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Document;
import com.creditscoring.entity.ResponsableCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.StatutDocument;
import com.creditscoring.enums.TypeDocument;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.DocumentRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentVerificationServiceImpl
        implements DocumentVerificationService {

    private final DocumentRepository documentRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final UtilisateurRepository utilisateurRepository;

    // =====================================================
    // VERIFICATION DES DOCUMENTS
    // =====================================================

    @Override
    public DocumentVerificationResponse verifierDocuments(
            Long demandeId,
            String emailUtilisateur
    ) {

        // =================================================
        // 1. Récupérer la demande
        // =================================================

        DemandeCredit demande =
                demandeCreditRepository
                        .findById(demandeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande de crédit introuvable."
                                )
                        );

        // =================================================
        // 2. Récupérer utilisateur connecté
        // =================================================

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable."
                                )
                        );

        // =================================================
        // 3. Vérifier accès à la demande
        // =================================================

        verifierAccesDemande(
                demande,
                utilisateur
        );

        // =================================================
        // 4. Récupérer documents
        // =================================================

        List<Document> documents =
                documentRepository.findAll()
                        .stream()
                        .filter(document ->
                                document.getDemandeCredit() != null
                                        && demandeId.equals(
                                        document
                                                .getDemandeCredit()
                                                .getId()
                                )
                        )
                        .toList();

        // =================================================
        // 5. Types obligatoires
        // =================================================

        List<TypeDocument> typesObligatoires =
                List.of(
                        TypeDocument.CIN,
                        TypeDocument.BULLETIN_SALAIRE,
                        TypeDocument.ATTESTATION_TRAVAIL,
                        TypeDocument.RELEVE_BANCAIRE,
                        TypeDocument.CONTRAT_TRAVAIL
                );

        List<DocumentVerificationResponse.DocumentStatus>
                documentStatuses =
                new ArrayList<>();

        boolean complet = true;

        // =================================================
        // 6. Vérification
        // =================================================

        for (TypeDocument type :
                typesObligatoires) {

            Document document =
                    documents.stream()
                            .filter(d ->
                                    d.getTypeDocument()
                                            == type
                            )
                            .findFirst()
                            .orElse(null);

            boolean present =
                    document != null;

            StatutDocument statut =
                    document != null
                            ? document.getStatutDocument()
                            : null;

            String message;

            if (!present) {

                complet = false;

                message =
                        "Document manquant.";

            } else if (statut ==
                    StatutDocument.VALIDE) {

                message =
                        "Document présent et valide.";

            } else if (statut ==
                    StatutDocument.EN_ATTENTE) {

                complet = false;

                message =
                        "Document présent mais en attente de validation.";

            } else if (statut ==
                    StatutDocument.REFUSE) {

                complet = false;

                message =
                        "Document refusé.";

            } else if (statut ==
                    StatutDocument.EXPIRE) {

                complet = false;

                message =
                        "Document expiré.";

            } else {

                complet = false;

                message =
                        "Statut du document inconnu.";
            }

            documentStatuses.add(
                    DocumentVerificationResponse.DocumentStatus
                            .builder()
                            .typeDocument(type)
                            .present(present)
                            .statut(statut)
                            .message(message)
                            .build()
            );
        }

        // =================================================
        // 7. Retour
        // =================================================

        return DocumentVerificationResponse.builder()
                .demandeId(demandeId)
                .complet(complet)
                .documents(documentStatuses)
                .build();
    }

    // =====================================================
    // DOSSIER COMPLET
    // =====================================================

    @Override
    public boolean isComplet(
            Long demandeId,
            String emailUtilisateur
    ) {

        return verifierDocuments(
                demandeId,
                emailUtilisateur
        ).isComplet();
    }

    // =====================================================
    // VERIFICATION ACCES
    // =====================================================

    private void verifierAccesDemande(
            DemandeCredit demande,
            Utilisateur utilisateur
    ) {

        // -------------------------------------------------
        // CONSEILLER
        // -------------------------------------------------

        if (utilisateur instanceof Conseiller conseiller) {

            if (demande.getConseiller() == null
                    || !demande.getConseiller()
                    .getId()
                    .equals(conseiller.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "est affectée à un autre conseiller."
                );
            }

            return;
        }

        // -------------------------------------------------
        // RESPONSABLE
        // -------------------------------------------------

        if (utilisateur instanceof ResponsableCredit responsable) {

            if (demande.getResponsable() == null
                    || !demande.getResponsable()
                    .getId()
                    .equals(responsable.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "est affectée à un autre responsable."
                );
            }

            return;
        }

        // -------------------------------------------------
        // CLIENT
        // -------------------------------------------------

        if (utilisateur instanceof Client) {

            throw new RuntimeException(
                    "Accès interdit : la vérification "
                            + "des documents est réservée "
                            + "au personnel de crédit."
            );
        }

        throw new RuntimeException(
                "Accès interdit."
        );
    }
}