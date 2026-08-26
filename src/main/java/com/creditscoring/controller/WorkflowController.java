package com.creditscoring.controller;

import com.creditscoring.dto.request.WorkflowTransitionRequest;
import com.creditscoring.dto.reponse.SuiviDemandeResponse;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorkflowController {

    private final WorkflowService workflowService;

    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE_CREDIT', 'DIRECTEUR')")
    @PutMapping("/demandes/{demandeId}/transition")
    public ResponseEntity<String> transitionner(
            @PathVariable Long demandeId,
            @Valid @RequestBody WorkflowTransitionRequest request,
            Authentication authentication
    ) {

        String emailUtilisateur =
                authentication.getName();

        String roleUtilisateur =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority())
                        .orElse("");

        workflowService.transitionner(
                demandeId,
                request,
                emailUtilisateur,
                roleUtilisateur
        );

        return ResponseEntity.ok(
                "Transition effectuée avec succès."
        );
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE_CREDIT', 'DIRECTEUR')")
    @GetMapping("/demandes/{demandeId}/statut")
    public ResponseEntity<StatutDemande> getStatut(
            @PathVariable Long demandeId
    ) {

        return ResponseEntity.ok(
                workflowService.getStatutActuel(demandeId)
        );
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE_CREDIT', 'DIRECTEUR')")
    @GetMapping("/demandes/{demandeId}/suivi")
    public ResponseEntity<SuiviDemandeResponse> getSuivi(
            @PathVariable Long demandeId
    ) {

        return ResponseEntity.ok(
                workflowService.getSuivi(demandeId)
        );
    }
}