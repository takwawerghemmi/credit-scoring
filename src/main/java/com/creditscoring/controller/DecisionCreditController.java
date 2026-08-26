package com.creditscoring.controller;

import com.creditscoring.dto.reponse.DecisionResponse;
import com.creditscoring.dto.request.DecisionRequest;
import com.creditscoring.service.DecisionCreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decision-credit")
@RequiredArgsConstructor
public class DecisionCreditController {

    private final DecisionCreditService decisionCreditService;

    @PostMapping
    @PreAuthorize("hasRole('DIRECTEUR')")
    public ResponseEntity<DecisionResponse> prendreDecision(
            @Valid @RequestBody DecisionRequest request,
            Authentication authentication
    ) {

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority())
                        .orElse("");

        String emailDirecteur =
                authentication.getName();

        return ResponseEntity.ok(
                decisionCreditService.prendreDecision(
                        request,
                        role,
                        emailDirecteur
                )
        );
    }
}
