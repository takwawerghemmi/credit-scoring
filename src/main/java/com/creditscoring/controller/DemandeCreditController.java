package com.creditscoring.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import com.creditscoring.dto.request.DemandeCreditRequest;
import com.creditscoring.dto.reponse.DemandeCreditResponse;
import com.creditscoring.service.DemandeCreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DemandeCreditController {

    private final DemandeCreditService service;

    @PostMapping
    public ResponseEntity<DemandeCreditResponse> creer(
            @Valid @RequestBody DemandeCreditRequest request,
            Authentication authentication) {

        String emailUtilisateur = authentication.getName();

        return ResponseEntity.ok(
                service.creer(request, emailUtilisateur)
        );
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/mes-demandes")
    public ResponseEntity<List<DemandeCreditResponse>> afficherMesDemandes(
            Authentication authentication) {

        String emailUtilisateur = authentication.getName();

        return ResponseEntity.ok(
                service.afficherMesDemandes(emailUtilisateur)
        );
    }

    @PreAuthorize("hasRole('CONSEILLER')")
    @GetMapping
    public ResponseEntity<List<DemandeCreditResponse>> afficherToutes(
            Authentication authentication) {

        String emailUtilisateur = authentication.getName();

        return ResponseEntity.ok(
                service.afficherParConseiller(emailUtilisateur)
        );
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    @GetMapping("/{id}")
    public ResponseEntity<DemandeCreditResponse> trouver(
            @PathVariable Long id,
            Authentication authentication) {

        String emailUtilisateur = authentication.getName();
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        return ResponseEntity.ok(
                service.trouverParIdPourUtilisateur(
                        id,
                        emailUtilisateur,
                        role
                )
        );
    }

    @PreAuthorize("hasRole('CONSEILLER')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<DemandeCreditResponse>> afficherParClient(
            @PathVariable Long clientId) {

        return ResponseEntity.ok(service.afficherParClient(clientId));
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    @PutMapping("/{id}")
    public ResponseEntity<DemandeCreditResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody DemandeCreditRequest request,
            Authentication authentication) {

        String emailUtilisateur = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        return ResponseEntity.ok(
                service.modifierPourUtilisateur(
                        id,
                        request,
                        emailUtilisateur,
                        role
                )
        );
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'CONSEILLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(
            @PathVariable Long id,
            Authentication authentication) {

        String emailUtilisateur = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("");

        service.supprimerPourUtilisateur(
                id,
                emailUtilisateur,
                role
        );

        return ResponseEntity.ok("Demande supprimée.");
    }
}