package com.creditscoring.controller;

import com.creditscoring.dto.reponse.DocumentResponse;
import com.creditscoring.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DocumentController {

    private final DocumentService service;


    // =========================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(

            @RequestParam("file") MultipartFile file,

            @RequestParam("nom") String nom,

            @RequestParam("type") String type,

            @RequestParam("demandeCreditId") Long demandeCreditId

    ) {

        DocumentResponse response = service.ajouterFichier(
                file,
                nom,
                type,
                demandeCreditId
        );

        return ResponseEntity.ok(response);
    }


    // =========================================================
    // GET ALL DOCUMENTS
    // =========================================================
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> afficherTous() {

        return ResponseEntity.ok(service.afficherTous());
    }


    // =========================================================
    // DELETE
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimer(@PathVariable Long id) {

        service.supprimer(id);

        return ResponseEntity.ok("Document supprimé.");
    }


    // =========================================================
    // PREVIEW
    // GET /api/documents/{id}/preview
    // =========================================================
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(
            @PathVariable Long id
    ) {

        Resource resource = service.preview(id);

        String contentType = "application/octet-stream";

        try {

            String detectedType = Files.probeContentType(
                    Paths.get(resource.getFile().getAbsolutePath())
            );

            if (detectedType != null) {
                contentType = detectedType;
            }

        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }


    // =========================================================
    // DOWNLOAD
    // GET /api/documents/{id}/download
    // =========================================================
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id
    ) {

        Resource resource = service.download(id);

        String contentType = "application/octet-stream";

        try {

            String detectedType = Files.probeContentType(
                    Paths.get(resource.getFile().getAbsolutePath())
            );

            if (detectedType != null) {
                contentType = detectedType;
            }

        } catch (Exception ignored) {
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }
}