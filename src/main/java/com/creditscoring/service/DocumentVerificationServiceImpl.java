package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentVerificationResponse;
import com.creditscoring.entity.Document;
import com.creditscoring.enums.StatutDocument;
import com.creditscoring.enums.TypeDocument;
import com.creditscoring.repository.DocumentRepository;
import com.creditscoring.repository.DemandeCreditRepository;
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

    @Override
    public DocumentVerificationResponse verifierDocuments(
            Long demandeId
    ) {

        // =====================================================
        // 1. Vérifier que la demande existe
        // =====================================================

        if (!demandeCreditRepository.existsById(demandeId)) {
            throw new RuntimeException(
                    "Demande de crédit introuvable."
            );
        }

        // =====================================================
        // 2. Récupérer les documents de la demande
        // =====================================================

        List<Document> documents =
                documentRepository.findAll()
                        .stream()
                        .filter(document ->
                                document.getDemandeCredit() != null
                                        && demandeId.equals(
                                        document.getDemandeCredit().getId()
                                )
                        )
                        .toList();

        // =====================================================
        // 3. Types obligatoires
        // =====================================================

        List<TypeDocument> typesObligatoires =
                List.of(
                        TypeDocument.CIN,
                        TypeDocument.BULLETIN_SALAIRE,
                        TypeDocument.RELEVE_BANCAIRE,
                        TypeDocument.ATTESTATION_TRAVAIL
                );

        List<DocumentVerificationResponse.DocumentStatus>
                documentStatuses =
                new ArrayList<>();

        boolean complet = true;

        // =====================================================
        // 4. Vérifier chaque document obligatoire
        // =====================================================

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

        // =====================================================
        // 5. Retour
        // =====================================================

        return DocumentVerificationResponse.builder()
                .demandeId(demandeId)
                .complet(complet)
                .documents(documentStatuses)
                .build();
    }

    @Override
    public boolean isComplet(
            Long demandeId
    ) {

        return verifierDocuments(
                demandeId
        ).isComplet();
    }
}