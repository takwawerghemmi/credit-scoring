package com.creditscoring.controller;

import com.creditscoring.dto.reponse.ResponsableRisqueResponse;
import com.creditscoring.service.ResponsableRisqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<ResponsableRisqueResponse>> dossiersARisque(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                responsableRisqueService.getDossiersARisque(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/dossiers/priorites")
    @PreAuthorize("hasRole('RESPONSABLE_CREDIT')")
    public ResponseEntity<List<ResponsableRisqueResponse>> dossiersPrioritaires(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                responsableRisqueService.getDossiersPrioritaires(
                        authentication.getName()
                )
        );
    }
}