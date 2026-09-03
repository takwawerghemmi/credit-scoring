package com.creditscoring.controller;

import com.creditscoring.dto.request.GarantieRequest;
import com.creditscoring.dto.reponse.GarantieResponse;
import com.creditscoring.service.GarantieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garanties")
@RequiredArgsConstructor
@CrossOrigin("*")
public class GarantieController {

    private final GarantieService service;

    // =====================================================
    // AJOUTER
    // =====================================================

    @PostMapping
    public ResponseEntity<GarantieResponse> ajouter(
            @Valid @RequestBody GarantieRequest request,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("");

        return ResponseEntity.ok(
                service.ajouter(
                        request,
                        email,
                        role
                )
        );
    }

    // =====================================================
    // AFFICHER SEULEMENT CE QUI CONCERNE L'UTILISATEUR
    // =====================================================

    @GetMapping
    public ResponseEntity<List<GarantieResponse>> afficherToutes(
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("");

        return ResponseEntity.ok(
                service.afficherToutes(
                        email,
                        role
                )
        );
    }

    // =====================================================
    // AFFICHER PAR DEMANDE
    // =====================================================

    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<List<GarantieResponse>>
    afficherParDemande(
            @PathVariable Long demandeId,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("");

        return ResponseEntity.ok(
                service.afficherParDemande(
                        demandeId,
                        email,
                        role
                )
        );
    }

    // =====================================================
    // SUPPRIMER
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(
            @PathVariable Long id,
            Authentication authentication
    ) {

        String email =
                authentication.getName();

        String role =
                authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("");

        service.supprimer(
                id,
                email,
                role
        );

        return ResponseEntity.ok(
                "Garantie supprimée."
        );
    }
}