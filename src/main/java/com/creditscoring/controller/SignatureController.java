package com.creditscoring.controller;

import com.creditscoring.entity.Contrat;
import com.creditscoring.service.ContratService;
import com.creditscoring.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/signature")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SignatureController {

    private final SignatureService signatureService;
    private final ContratService contratService;

    @PostMapping("/{contratId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<String> signer(
            @PathVariable Long contratId,
            Authentication authentication
    ) {

        Contrat contrat =
                contratService.getContratById(
                        contratId
                );

        // =====================================================
        // Vérifier que le contrat appartient au client connecté
        // =====================================================

        if (contrat.getDemandeCredit() == null
                || contrat.getDemandeCredit()
                .getClient() == null
                || !contrat.getDemandeCredit()
                .getClient()
                .getEmail()
                .equals(
                        authentication.getName()
                )) {

            throw new RuntimeException(
                    "Accès interdit : ce contrat n'appartient pas au client connecté."
            );
        }

        return ResponseEntity.ok(
                signatureService.signerContrat(
                        contrat
                )
        );
    }
}