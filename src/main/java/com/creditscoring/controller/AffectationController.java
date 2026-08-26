package com.creditscoring.controller;

import com.creditscoring.service.AffectationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/affectation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AffectationController {

    private final AffectationService affectationService;

    @PostMapping("/{demandeId}/{conseillerId}")
    public String affecter(@PathVariable Long demandeId,
                           @PathVariable Long conseillerId) {

        return affectationService.affecterDemande(demandeId, conseillerId);
    }
}