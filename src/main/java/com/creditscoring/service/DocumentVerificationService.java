package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentVerificationResponse;

public interface DocumentVerificationService {

    DocumentVerificationResponse verifierDocuments(
            Long demandeId,
            String emailUtilisateur
    );

    boolean isComplet(
            Long demandeId,
            String emailUtilisateur
    );
}