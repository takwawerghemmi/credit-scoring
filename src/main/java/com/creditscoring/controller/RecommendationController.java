package com.creditscoring.controller;

import com.creditscoring.dto.reponse.RecommendationResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final DemandeCreditRepository demandeCreditRepository;

    @GetMapping("/{demandeId}")
    public ResponseEntity<RecommendationResponse> recommander(
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
                recommendationService
                        .genererRecommandations(demande)
        );
    }
}
