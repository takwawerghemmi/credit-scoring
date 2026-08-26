package com.creditscoring.controller;

import com.creditscoring.dto.request.CreditScoreRequest;
import com.creditscoring.dto.reponse.CreditScoreResponse;
import com.creditscoring.service.CreditScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/credit-scores")
@RequiredArgsConstructor
public class CreditScoreController {



    private final CreditScoreService creditScoreService;
    @PreAuthorize("hasAnyRole('CONSEILLER', 'RESPONSABLE_CREDIT')")

    @PostMapping("/calculer")
    public CreditScoreResponse calculerScore(
            @Valid @RequestBody CreditScoreRequest request) {

        return creditScoreService.calculerScore(request);
    }
    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE_CREDIT', 'DIRECTEUR')")

    @GetMapping
    public List<CreditScoreResponse> getAllScores() {
        return creditScoreService.getAllScores();
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE_CREDIT', 'DIRECTEUR')")

    @GetMapping("/{id}")
    public CreditScoreResponse getScore(@PathVariable Long id) {
        return creditScoreService.getScore(id);
    }

}
