package com.creditscoring.controller;

import com.creditscoring.dto.request.ResponsableValidationRequest;
import com.creditscoring.service.DoubleValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/double-validation")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DoubleValidationController {

    private final DoubleValidationService doubleValidationService;

    // =========================================================
    // CONSEILLER - PREMIERE VALIDATION
    // =========================================================

    @PreAuthorize("hasRole('CONSEILLER')")
    @PostMapping("/premiere/{id}")
    public ResponseEntity<String> premiereValidation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String emailValidateur =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(authority ->
                                authority.getAuthority())
                        .orElse("");

        return ResponseEntity.ok(
                doubleValidationService.premiereValidation(
                        id,
                        emailValidateur,
                        role
                )
        );
    }



    // =========================================================
    // RESPONSABLE - NOUVELLE VALIDATION METIER
    // ACCEPTER / REFUSER + COMMENTAIRE
    // =========================================================

    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    @PostMapping("/responsable/{id}/validation")
    public ResponseEntity<String> validationResponsable(
            @PathVariable Long id,
            @Valid @RequestBody ResponsableValidationRequest request,
            Authentication authentication
    ) {

        String emailValidateur =
                authentication.getName();

        return ResponseEntity.ok(
                doubleValidationService.validationResponsable(
                        id,
                        request,
                        emailValidateur
                )
        );
    }
}