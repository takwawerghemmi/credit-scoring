package com.creditscoring.controller;

import com.creditscoring.entity.Contrat;
import com.creditscoring.service.ContratPdfService;
import com.creditscoring.service.ContratService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contrat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ContratPdfController {

    private final ContratPdfService contratPdfService;
    private final ContratService contratService;

    @GetMapping("/pdf/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','DIRECTEUR','ADMIN')")
    public ResponseEntity<byte[]> genererPdf(
            @PathVariable Long id
    ) {

        Contrat contrat =
                contratService.getContratById(id);

        byte[] pdf =
                contratPdfService.genererContratPdf(contrat);

        String numero =
                contrat.getNumeroContrat() != null
                        ? contrat.getNumeroContrat()
                        : String.valueOf(id);

        String filename =
                "Contrat_CREDITNOVA_" + numero + ".pdf";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                )
                .contentType(
                        MediaType.APPLICATION_PDF
                )
                .contentLength(pdf.length)
                .body(pdf);
    }
}