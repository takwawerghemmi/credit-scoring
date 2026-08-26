package com.creditscoring.controller;

import com.creditscoring.dto.reponse.ResponsableRisqueResponse;
import com.creditscoring.service.ResponsableRisqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responsable")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResponsableRisqueController {

    private final ResponsableRisqueService responsableRisqueService;

    @GetMapping("/dossiers-a-risque")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<
            List<ResponsableRisqueResponse>
            > dossiersARisque() {

        return ResponseEntity.ok(
                responsableRisqueService
                        .getDossiersARisque()
        );
    }

    @GetMapping("/dossiers/priorites")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<
            List<ResponsableRisqueResponse>
            > dossiersPrioritaires() {

        return ResponseEntity.ok(
                responsableRisqueService
                        .getDossiersPrioritaires()
        );
    }
}