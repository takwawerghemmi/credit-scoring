package com.creditscoring.controller;

import com.creditscoring.dto.request.PaiementRequest;
import com.creditscoring.dto.reponse.PaiementResponse;
import com.creditscoring.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaiementController {

    private final PaiementService paiementService;

    // =====================================================
    // CRÉER UNE TRANSACTION
    // =====================================================

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaiementResponse> creerPaiement(
            @RequestBody PaiementRequest request
    ) {

        return ResponseEntity.ok(
                paiementService.creerPaiement(
                        request
                )
        );
    }

    // =====================================================
    // CONFIRMER
    // =====================================================

    @PostMapping("/{id}/confirmer")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaiementResponse>
    confirmerPaiement(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                paiementService.confirmerPaiement(
                        id
                )
        );
    }

    // =====================================================
    // ÉCHEC
    // =====================================================

    @PostMapping("/{id}/echouer")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<PaiementResponse>
    echouerPaiement(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                paiementService.echouerPaiement(
                        id
                )
        );
    }

    // =====================================================
    // GET ALL
    // =====================================================

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('CLIENT','CONSEILLER','RESPONSABLE','DIRECTEUR','ADMIN')"
    )
    public ResponseEntity<List<PaiementResponse>>
    getAllPaiements() {

        return ResponseEntity.ok(
                paiementService.getAllPaiements()
        );
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','CONSEILLER','RESPONSABLE','DIRECTEUR','ADMIN')"
    )
    public ResponseEntity<PaiementResponse>
    getPaiementById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                paiementService.getPaiementById(
                        id
                )
        );
    }

    // =====================================================
    // GET PAIEMENTS PAR CONTRAT
    // =====================================================

    @GetMapping("/contrat/{contratId}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','CONSEILLER','RESPONSABLE','DIRECTEUR','ADMIN')"
    )
    public ResponseEntity<List<PaiementResponse>>
    getPaiementsByContrat(
            @PathVariable Long contratId
    ) {

        return ResponseEntity.ok(
                paiementService.getPaiementsByContrat(
                        contratId
                )
        );
    }

    // =====================================================
    // DELETE
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTEUR')")
    public ResponseEntity<String> deletePaiement(
            @PathVariable Long id
    ) {

        paiementService.deletePaiement(
                id
        );

        return ResponseEntity.ok(
                "Paiement supprimé avec succès."
        );
    }
}