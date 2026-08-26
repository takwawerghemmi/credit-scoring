package com.creditscoring.controller;

import com.creditscoring.dto.request.AgenceRequest;
import com.creditscoring.dto.reponse.AgenceResponse;
import com.creditscoring.service.AgenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agences")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AgenceController {

    private final AgenceService agenceService;

    @PostMapping
    public ResponseEntity<AgenceResponse> creerAgence(
            @Valid @RequestBody AgenceRequest request) {

        return ResponseEntity.ok(
                agenceService.creerAgence(request));
    }

    @GetMapping
    public ResponseEntity<List<AgenceResponse>> getAll() {

        return ResponseEntity.ok(
                agenceService.obtenirToutesLesAgences());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgenceResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                agenceService.obtenirAgence(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgenceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AgenceRequest request) {

        return ResponseEntity.ok(
                agenceService.modifierAgence(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        agenceService.supprimerAgence(id);

        return ResponseEntity.ok("Agence supprimée avec succès.");
    }
}