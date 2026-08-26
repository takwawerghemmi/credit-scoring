package com.creditscoring.controller;

import com.creditscoring.dto.reponse.PredictionResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;
    private final DemandeCreditRepository demandeCreditRepository;

    @GetMapping("/{demandeId}")
    public ResponseEntity<PredictionResponse> predire(
            @PathVariable Long demandeId
    ) {

        DemandeCredit demande =
                demandeCreditRepository.findById(demandeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        return ResponseEntity.ok(
                predictionService.predire(demande)
        );
    }
}
