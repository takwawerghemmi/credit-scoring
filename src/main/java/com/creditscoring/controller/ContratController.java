package com.creditscoring.controller;

import com.creditscoring.dto.request.ContratRequest;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contrats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContratController {

    private final ContratService contratService;
    private final DemandeCreditRepository demandeCreditRepository;
    private final UtilisateurRepository utilisateurRepository;

    // =========================================================
    // CREATION MANUELLE
    // =========================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTEUR')")
    public ResponseEntity<Contrat> creerContrat(
            @RequestBody ContratRequest request
    ) {
        return ResponseEntity.ok(
                contratService.creerContrat(request)
        );
    }

    // =========================================================
    // LISTE DES CONTRATS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTEUR')")
    public ResponseEntity<List<Contrat>> getAllContrats() {
        return ResponseEntity.ok(
                contratService.getAllContrats()
        );
    }

    // =========================================================
    // CONTRAT PAR ID
    // =========================================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','ADMIN','DIRECTEUR')"
    )
    public ResponseEntity<Contrat> getContratById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                contratService.getContratById(id)
        );
    }

    // =========================================================
    // CONTRAT PAR DEMANDE
    // =========================================================

    @GetMapping("/demande/{demandeId}")
    @PreAuthorize(
            "hasAnyRole('CLIENT','ADMIN','DIRECTEUR')"
    )
    public ResponseEntity<Contrat> getContratByDemande(
            @PathVariable Long demandeId
    ) {
        return ResponseEntity.ok(
                contratService.getContratByDemandeId(
                        demandeId
                )
        );
    }

    // =========================================================
    // MODIFICATION
    // =========================================================

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','DIRECTEUR')"
    )
    public ResponseEntity<Contrat> updateContrat(
            @PathVariable Long id,
            @RequestBody ContratRequest request
    ) {
        return ResponseEntity.ok(
                contratService.updateContrat(
                        id,
                        request
                )
        );
    }

    // =========================================================
    // SUPPRESSION
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteContrat(
            @PathVariable Long id
    ) {

        contratService.deleteContrat(id);

        return ResponseEntity.ok(
                "Contrat supprimé avec succès."
        );
    }

    // =========================================================
    // CREATION AUTOMATIQUE POUR UNE DEMANDE DEJA APPROUVEE
    // =========================================================
    //
    // Cette route sert à rattraper la demande #2 qui était
    // déjà APPROUVEE avant qu'on ajoute la création automatique.
    //
    // Après cette correction, les nouvelles approbations
    // passent directement par DecisionCreditService.
    // =========================================================

    @PostMapping("/automatique/demande/{demandeId}")
    @PreAuthorize("hasAnyRole('DIRECTEUR','ADMIN')")
    public ResponseEntity<Contrat> creerAutomatiquement(
            @PathVariable Long demandeId,
            Authentication authentication
    ) {

        DemandeCredit demande =
                demandeCreditRepository.findById(
                        demandeId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );
        if (demande.getStatut() == null
                || (
                !demande.getStatut().name().equals("APPROUVEE")
                        && !demande.getStatut().name().equals("CONTRAT_SIGNE")
        )) {

            throw new RuntimeException(
                    "La demande doit être APPROUVEE ou CONTRAT_SIGNE avant la création du contrat."
            );
        }
        Utilisateur utilisateur =
                utilisateurRepository.findByEmail(
                        authentication.getName()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable."
                        )
                );

        Contrat contrat =
                contratService.creerContratAutomatiquement(
                        demande,
                        utilisateur
                );

        return ResponseEntity.ok(
                contrat
        );
    }
}