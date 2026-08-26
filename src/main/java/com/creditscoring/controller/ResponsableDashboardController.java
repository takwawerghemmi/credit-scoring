package com.creditscoring.controller;

import com.creditscoring.dto.reponse.ResponsableDashboardResponse;
import com.creditscoring.service.ResponsableDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsable")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResponsableDashboardController {

    private final ResponsableDashboardService responsableDashboardService;

    /**
     * Dashboard complet du Responsable.
     * Contient KPI + dossiers à contrôler + derniers dossiers.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<ResponsableDashboardResponse> dashboard() {

        return ResponseEntity.ok(
                responsableDashboardService.getDashboard()
        );
    }

    /**
     * Liste uniquement les dossiers qui attendent
     * la deuxième validation du Responsable.
     */
    @GetMapping("/demandes-a-controler")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<
            List<ResponsableDashboardResponse.DossierResponsable>
            > demandesAControler() {

        ResponsableDashboardResponse dashboard =
                responsableDashboardService.getDashboard();

        return ResponseEntity.ok(
                dashboard.getDossiersAControler()
        );
    }

    /**
     * Détail d'un dossier.
     */
    @GetMapping("/demandes/{id}")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<
            ResponsableDashboardResponse.DossierResponsable
            > dossier(@PathVariable Long id) {

        return ResponseEntity.ok(
                responsableDashboardService.getDossier(id)
        );
    }
}