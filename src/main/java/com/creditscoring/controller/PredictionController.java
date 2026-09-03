package com.creditscoring.controller;

import com.creditscoring.dto.reponse.PredictionResponse;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.ResponsableCredit;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;
    private final DemandeCreditRepository demandeCreditRepository;
    private final UtilisateurRepository utilisateurRepository;

    @GetMapping("/{demandeId}")
    public ResponseEntity<PredictionResponse> predire(
            @PathVariable Long demandeId,
            Authentication authentication
    ) {

        DemandeCredit demande =
                demandeCreditRepository.findById(demandeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        Utilisateur utilisateur =
                utilisateurRepository.findByEmail(
                        authentication.getName()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable."
                        )
                );

        verifierAcces(demande, utilisateur);

        return ResponseEntity.ok(
                predictionService.predire(demande)
        );
    }

    private void verifierAcces(
            DemandeCredit demande,
            Utilisateur utilisateur
    ) {

        if (utilisateur instanceof Client client) {

            if (demande.getClient() == null
                    || !demande.getClient()
                    .getId()
                    .equals(client.getId())) {

                throw new RuntimeException(
                        "Accès interdit à cette demande."
                );
            }

            return;
        }

        if (utilisateur instanceof Conseiller conseiller) {

            if (demande.getConseiller() == null
                    || !demande.getConseiller()
                    .getId()
                    .equals(conseiller.getId())) {

                throw new RuntimeException(
                        "Accès interdit : demande affectée à un autre conseiller."
                );
            }

            return;
        }

        if (utilisateur instanceof ResponsableCredit responsable) {

            if (demande.getResponsable() == null
                    || !demande.getResponsable()
                    .getId()
                    .equals(responsable.getId())) {

                throw new RuntimeException(
                        "Accès interdit : demande affectée à un autre responsable."
                );
            }

            return;
        }

        throw new RuntimeException("Accès interdit.");
    }
}