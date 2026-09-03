package com.creditscoring.controller;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.request.UpdateUtilisateurRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;
import com.creditscoring.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;


    // =========================================================
    // CREER CLIENT
    // =========================================================

    @PostMapping("/client")
    public ResponseEntity<UtilisateurResponse> creerClient(
            @Valid
            @RequestBody BanqueRequest.RegisterClientRequest request) {

        return ResponseEntity.ok(
                utilisateurService.creerClient(request)
        );
    }


    // =========================================================
    // GET TOUS LES UTILISATEURS
    // =========================================================

    @GetMapping
    public ResponseEntity<List<UtilisateurResponse>> getAll() {

        return ResponseEntity.ok(
                utilisateurService.obtenirTousLesUtilisateurs()
        );
    }


    // =========================================================
    // GET UTILISATEUR PAR ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                utilisateurService.obtenirUtilisateur(id)
        );
    }


    // =========================================================
    // GET PAR EMAIL
    // =========================================================

    @GetMapping("/email/{email}")
    public ResponseEntity<UtilisateurResponse> getByEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(
                utilisateurService.obtenirParEmail(email)
        );
    }


    // =========================================================
    // MODIFIER UTILISATEUR
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<UtilisateurResponse> update(
            @PathVariable Long id,
            @Valid
            @RequestBody UpdateUtilisateurRequest request) {

        return ResponseEntity.ok(
                utilisateurService.modifierUtilisateur(
                        id,
                        request
                )
        );
    }


    // =========================================================
    // SUPPRIMER UTILISATEUR
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id) {

        utilisateurService.supprimerUtilisateur(id);

        return ResponseEntity.ok(
                "Utilisateur supprimé avec succès."
        );
    }
}