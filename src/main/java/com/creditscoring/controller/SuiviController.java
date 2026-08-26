package com.creditscoring.controller;

import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.service.SuiviDemandeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suivi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuiviController {

    private final SuiviDemandeService suiviDemandeService;
    private final DemandeCreditRepository demandeCreditRepository;

    @GetMapping("/{id}")
    public ResponseEntity<String> suivreDemande(@PathVariable Long id) {

        DemandeCredit demande = demandeCreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));

        String resultat = suiviDemandeService.suivreDemande(demande);

        return ResponseEntity.ok(resultat);
    }
}