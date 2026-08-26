package com.creditscoring.mapper;

import com.creditscoring.dto.reponse.DocumentResponse;
import com.creditscoring.entity.Document;

public class DocumentMapper {

    private DocumentMapper() {
    }

    public static DocumentResponse toResponse(Document document) {

        return DocumentResponse.builder()
                .id(document.getId())
                .nom(document.getNom())
                .type(document.getType())
                .chemin(document.getCheminFichier())
                .demandeCreditId(
                        document.getDemandeCredit() != null
                                ? document.getDemandeCredit().getId()
                                : null
                )
                .build();
    }
}