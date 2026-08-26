
package com.creditscoring.controller;

import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.service.RisqueAnalyseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risque")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RisqueController {

    private final RisqueAnalyseService risqueAnalyseService;
    private final DemandeCreditRepository demandeCreditRepository;

    @GetMapping("/analyser/{id}")
    public ResponseEntity<String> analyserRisque(@PathVariable Long id) {

        DemandeCredit demande = demandeCreditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande de crédit introuvable."));

        String resultat = risqueAnalyseService.analyserRisque(demande);

        return ResponseEntity.ok(resultat);
    }
}