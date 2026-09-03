package com.creditscoring.controller;

import com.creditscoring.dto.request.CreditScoreRequest;
import com.creditscoring.dto.reponse.CreditScoreResponse;
import com.creditscoring.service.CreditScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-scores")
@RequiredArgsConstructor
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    // =====================================================
    // CALCUL SCORE
    // =====================================================

    @PreAuthorize(
            "hasAnyRole('CONSEILLER', 'RESPONSABLE_CREDIT')"
    )
    @PostMapping("/calculer")
    public CreditScoreResponse calculerScore(
            @Valid @RequestBody CreditScoreRequest request,
            Authentication authentication
    ) {

        return creditScoreService.calculerScore(
                request,
                authentication.getName()
        );
    }

    // =====================================================
    // GET ALL SCORES ACCESSIBLES
    // =====================================================

    @PreAuthorize(
            "hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE_CREDIT')"
    )
    @GetMapping
    public List<CreditScoreResponse> getAllScores(
            Authentication authentication
    ) {

        return creditScoreService.getAllScores(
                authentication.getName()
        );
    }

    // =====================================================
    // GET SCORE PAR ID
    // =====================================================

    @PreAuthorize(
            "hasAnyRole('CLIENT', 'CONSEILLER', 'RESPONSABLE_CREDIT')"
    )
    @GetMapping("/{id}")
    public CreditScoreResponse getScore(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return creditScoreService.getScore(
                id,
                authentication.getName()
        );
    }
}