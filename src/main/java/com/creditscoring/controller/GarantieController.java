package com.creditscoring.controller;

import com.creditscoring.dto.request.GarantieRequest;
import com.creditscoring.dto.reponse.GarantieResponse;
import com.creditscoring.service.GarantieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/garanties")
@RequiredArgsConstructor
@CrossOrigin("*")
public class GarantieController {

    private final GarantieService service;

    @PostMapping
    public ResponseEntity<GarantieResponse> ajouter(
            @Valid @RequestBody GarantieRequest request) {

        return ResponseEntity.ok(service.ajouter(request));
    }

    @GetMapping
    public ResponseEntity<List<GarantieResponse>> afficherToutes() {

        return ResponseEntity.ok(service.afficherToutes());
    }
    @GetMapping("/demande/{demandeId}")
    public ResponseEntity<List<GarantieResponse>> afficherParDemande(
            @PathVariable Long demandeId
    ) {

        return ResponseEntity.ok(
                service.afficherParDemande(demandeId)
        );
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable Long id) {

        service.supprimer(id);
        return ResponseEntity.ok("Garantie supprimée.");
    }
}