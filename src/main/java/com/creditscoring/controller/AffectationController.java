package com.creditscoring.controller;

import com.creditscoring.service.AffectationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/affectation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AffectationController {

    private final AffectationService affectationService;

    @PostMapping("/{demandeId}/{conseillerId}/{responsableId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String affecter(
            @PathVariable Long demandeId,
            @PathVariable Long conseillerId,
            @PathVariable Long responsableId) {

        return affectationService.affecterDemande(
                demandeId,
                conseillerId,
                responsableId
        );
    }
}
