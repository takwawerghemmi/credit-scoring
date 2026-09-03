package com.creditscoring.controller;

import com.creditscoring.dto.reponse.ResponsableDashboardResponse;
import com.creditscoring.service.ResponsableDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsable")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResponsableDashboardController {

    private final ResponsableDashboardService responsableDashboardService;

    /**
     * Dashboard complet du Responsable connecté.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<ResponsableDashboardResponse> dashboard(
            Authentication authentication) {

        return ResponseEntity.ok(
                responsableDashboardService.getDashboard(
                        authentication.getName()
                )
        );
    }

    /**
     * Liste uniquement les dossiers du Responsable connecté
     * qui attendent sa deuxième validation.
     */
    @GetMapping("/demandes-a-controler")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<
            List<ResponsableDashboardResponse.DossierResponsable>
            > demandesAControler(
            Authentication authentication) {

        ResponsableDashboardResponse dashboard =
                responsableDashboardService.getDashboard(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                dashboard.getDossiersAControler()
        );
    }

    /**
     * Détail d'un dossier appartenant au Responsable connecté.
     */
    @GetMapping("/demandes/{id}")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<
            ResponsableDashboardResponse.DossierResponsable
            > dossier(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                responsableDashboardService.getDossier(
                        id,
                        authentication.getName()
                )
        );
    }
}