package com.creditscoring.controller;

import com.creditscoring.dto.reponse.DocumentVerificationResponse;
import com.creditscoring.service.DocumentVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-verification")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DocumentVerificationController {

    private final DocumentVerificationService documentVerificationService;


    @GetMapping("/{demandeId}")
    public ResponseEntity<DocumentVerificationResponse> verifierDocuments(
            @PathVariable Long demandeId
    ) {

        DocumentVerificationResponse response =
                documentVerificationService.verifierDocuments(demandeId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{demandeId}/complet")
    public ResponseEntity<Boolean> isComplet(
            @PathVariable Long demandeId
    ) {

        boolean complet =
                documentVerificationService.isComplet(demandeId);

        return ResponseEntity.ok(complet);
    }
}
