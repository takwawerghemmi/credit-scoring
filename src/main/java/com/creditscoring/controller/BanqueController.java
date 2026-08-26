package com.creditscoring.controller;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.reponse.BanqueResponse;
import com.creditscoring.service.BanqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banques")
@RequiredArgsConstructor
@CrossOrigin("*")
public class BanqueController {

    private final BanqueService banqueService;

    @PostMapping
    public ResponseEntity<BanqueResponse> ajouter(
            @Valid @RequestBody BanqueRequest request){

        return ResponseEntity.ok(banqueService.ajouter(request));

    }

    @GetMapping
    public ResponseEntity<List<BanqueResponse>> afficherToutes(){

        return ResponseEntity.ok(banqueService.afficherToutes());

    }

    @GetMapping("/{id}")
    public ResponseEntity<BanqueResponse> trouverParId(@PathVariable Long id){

        return ResponseEntity.ok(banqueService.trouverParId(id));

    }

    @PutMapping("/{id}")
    public ResponseEntity<BanqueResponse> modifier(
            @PathVariable Long id,
            @Valid @RequestBody BanqueRequest request){

        return ResponseEntity.ok(banqueService.modifier(id,request));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable Long id){

        banqueService.supprimer(id);

        return ResponseEntity.ok("Banque supprimée avec succès.");

    }

}