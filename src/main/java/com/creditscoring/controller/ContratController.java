package com.creditscoring.controller;

import com.creditscoring.entity.Contrat;
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

    // =========================================================
    // RESPONSABLE
    // VOIR SES CONTRATS
    // =========================================================

    @GetMapping("/responsable/mes-contrats")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<List<Contrat>> mesContratsResponsable(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                contratService.getContratsDuResponsable(
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // RESPONSABLE
    // VOIR UN CONTRAT
    // =========================================================

    @GetMapping("/responsable/{id}")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<Contrat> getContratResponsable(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Contrat contrat =
                contratService.getContratById(
                        id
                );

        if (contrat.getUtilisateur() == null
                || !contrat.getUtilisateur()
                .getEmail()
                .equals(
                        authentication.getName()
                )) {

            throw new RuntimeException(
                    "Accès interdit : ce contrat appartient à un autre Responsable."
            );
        }

        return ResponseEntity.ok(
                contrat
        );
    }

    // =========================================================
    // RESPONSABLE
    // VOIR CONTRAT PAR DEMANDE
    // =========================================================

    @GetMapping("/responsable/demande/{demandeId}")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<Contrat> getContratResponsableParDemande(
            @PathVariable Long demandeId,
            Authentication authentication
    ) {

        Contrat contrat =
                contratService.getContratByDemandeId(
                        demandeId
                );

        if (contrat.getUtilisateur() == null
                || !contrat.getUtilisateur()
                .getEmail()
                .equals(
                        authentication.getName()
                )) {

            throw new RuntimeException(
                    "Accès interdit : ce contrat appartient à un autre Responsable."
            );
        }

        return ResponseEntity.ok(
                contrat
        );
    }

    // =========================================================
    // RESPONSABLE
    // ENVOYER AU CLIENT
    // =========================================================

    @PostMapping("/{id}/envoyer-client")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<String> envoyerAuClient(
            @PathVariable Long id,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                contratService.envoyerContratAuClient(
                        id,
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // CLIENT
    // VOIR SES CONTRATS
    // =========================================================

    @GetMapping("/client/mes-contrats")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<Contrat>> mesContratsClient(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                contratService.getContratsDuClient(
                        authentication.getName()
                )
        );
    }

    // =========================================================
    // CLIENT
    // VOIR UN CONTRAT
    // =========================================================

    @GetMapping("/client/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Contrat> getContratClient(
            @PathVariable Long id,
            Authentication authentication
    ) {

        Contrat contrat =
                contratService.getContratById(
                        id
                );

        if (contrat.getDemandeCredit() == null
                || contrat.getDemandeCredit()
                .getClient() == null
                || !contrat.getDemandeCredit()
                .getClient()
                .getEmail()
                .equals(
                        authentication.getName()
                )) {

            throw new RuntimeException(
                    "Accès interdit : ce contrat n'appartient pas au client connecté."
            );
        }

        return ResponseEntity.ok(
                contrat
        );
    }

    // =========================================================
    // ADMIN
    // VOIR TOUS LES CONTRATS
    // =========================================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Contrat>> getAllContrats() {

        return ResponseEntity.ok(
                contratService.getAllContrats()
        );
    }

    // =========================================================
    // DELETE
    // ADMIN
    // =========================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteContrat(
            @PathVariable Long id
    ) {

        contratService.deleteContrat(
                id
        );

        return ResponseEntity.ok(
                "Contrat supprimé avec succès."
        );
    }
}