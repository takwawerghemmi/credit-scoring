package com.creditscoring.controller;

import com.creditscoring.dto.reponse.AmortissementResponse;
import com.creditscoring.dto.reponse.EcheanceResponse;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.ContratRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.service.EcheancierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/echeancier")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EcheancierController {

    private final EcheancierService echeancierService;
    private final DemandeCreditRepository demandeCreditRepository;
    private final ContratRepository contratRepository;

    // =====================================================
    // ANCIEN ENDPOINT : CALCUL THÉORIQUE
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','CONSEILLER','RESPONSABLE','DIRECTEUR','ADMIN')"
    )
    public ResponseEntity<List<AmortissementResponse>>
    generer(
            @PathVariable Long id
    ) {

        DemandeCredit demande =
                demandeCreditRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        return ResponseEntity.ok(
                echeancierService.genererEcheancier(
                        demande
                )
        );
    }

    // =====================================================
    // CRÉER LES ÉCHÉANCES RÉELLES
    // =====================================================

    @PostMapping("/contrat/{contratId}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','DIRECTEUR','ADMIN')"
    )
    public ResponseEntity<List<EcheanceResponse>>
    creerEcheances(
            @PathVariable Long contratId
    ) {

        Contrat contrat =
                contratRepository
                        .findById(contratId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contrat introuvable"
                                )
                        );

        return ResponseEntity.ok(
                echeancierService.creerEcheances(
                        contrat
                )
        );
    }

    // =====================================================
    // RÉCUPÉRER LES ÉCHÉANCES D'UN CONTRAT
    // =====================================================

    @GetMapping("/contrat/{contratId}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','CONSEILLER','RESPONSABLE','DIRECTEUR','ADMIN')"
    )
    public ResponseEntity<List<EcheanceResponse>>
    getEcheances(
            @PathVariable Long contratId
    ) {

        return ResponseEntity.ok(
                echeancierService.getEcheancesByContrat(
                        contratId
                )
        );
    }
}