package com.creditscoring.controller;

import com.creditscoring.dto.reponse.HistoriqueResponse;
import com.creditscoring.service.HistoriqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historiques")
public class HistoriqueController {

    private final HistoriqueService historiqueService;

    public HistoriqueController(HistoriqueService historiqueService) {
        this.historiqueService = historiqueService;
    }

    @GetMapping
    public ResponseEntity<List<HistoriqueResponse>> getAllHistoriques() {
        return ResponseEntity.ok(historiqueService.getAllHistoriques());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoriqueResponse> getHistoriqueById(@PathVariable Long id) {
        return ResponseEntity.ok(historiqueService.getHistoriqueById(id));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<HistoriqueResponse>> getHistoriqueUtilisateur(
            @PathVariable Long utilisateurId) {
        return ResponseEntity.ok(historiqueService.getHistoriqueUtilisateur(utilisateurId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerHistorique(@PathVariable Long id) {
        historiqueService.supprimerHistorique(id);
        return ResponseEntity.ok("Historique supprimé avec succès.");
    }
}