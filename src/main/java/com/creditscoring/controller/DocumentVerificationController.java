package com.creditscoring.controller;

import com.creditscoring.dto.reponse.DocumentVerificationResponse;
import com.creditscoring.service.DocumentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-verification")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DocumentVerificationController {

    private final DocumentVerificationService documentVerificationService;

    // =====================================================
    // VERIFICATION DES DOCUMENTS
    // =====================================================

    @GetMapping("/{demandeId}")
    @PreAuthorize(
            "hasAnyRole('CONSEILLER', 'RESPONSABLE_CREDIT')"
    )
    public ResponseEntity<DocumentVerificationResponse> verifierDocuments(
            @PathVariable Long demandeId,
            Authentication authentication
    ) {

        DocumentVerificationResponse response =
                documentVerificationService.verifierDocuments(
                        demandeId,
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // VERIFIER SI LE DOSSIER EST COMPLET
    // =====================================================

    @GetMapping("/{demandeId}/complet")
    @PreAuthorize(
            "hasAnyRole('CONSEILLER', 'RESPONSABLE_CREDIT')"
    )
    public ResponseEntity<Boolean> isComplet(
            @PathVariable Long demandeId,
            Authentication authentication
    ) {

        boolean complet =
                documentVerificationService.isComplet(
                        demandeId,
                        authentication.getName()
                );

        return ResponseEntity.ok(complet);
    }
}