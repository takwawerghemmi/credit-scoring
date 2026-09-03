package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentResponse;
import com.creditscoring.entity.Administrateur;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Document;
import com.creditscoring.entity.ResponsableCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.StatutDocument;
import com.creditscoring.enums.TypeDocument;
import com.creditscoring.mapper.DocumentMapper;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.DocumentRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DemandeCreditRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Value("${app.document.upload-dir:./uploads/documents}")
    private String uploadDir;

    // =========================================================
    // UPLOAD
    // =========================================================

    @Override
    public DocumentResponse ajouterFichier(
            MultipartFile file,
            String nom,
            String type,
            Long demandeCreditId
    ) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Veuillez sélectionner un fichier."
            );
        }

        DemandeCredit demande =
                demandeRepository.findById(demandeCreditId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande de crédit introuvable."
                                )
                        );

        // Vérification de sécurité
        verifierAccesDemande(demande);

        // Taille maximale : 10 MB
        long maxSize = 10 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new RuntimeException(
                    "Le fichier ne doit pas dépasser 10 MB."
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.trim().isEmpty()) {

            throw new RuntimeException(
                    "Nom de fichier invalide."
            );
        }

        originalFilename =
                Paths.get(originalFilename)
                        .getFileName()
                        .toString();

        String extension =
                getExtension(originalFilename);

        if (!extension.equals("pdf")
                && !extension.equals("png")
                && !extension.equals("jpg")
                && !extension.equals("jpeg")) {

            throw new RuntimeException(
                    "Format non autorisé. " +
                            "Seuls PDF, PNG, JPG et JPEG sont acceptés."
            );
        }

        TypeDocument typeDocument;

        try {

            typeDocument =
                    TypeDocument.valueOf(
                            type.trim().toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            throw new RuntimeException(
                    "Type de document invalide : " + type
            );
        }

        boolean existe =
                documentRepository
                        .existsByNomAndDemandeCreditId(
                                nom,
                                demande.getId()
                        );

        if (existe) {

            throw new RuntimeException(
                    "Ce document existe déjà pour cette demande."
            );
        }

        Path baseDirectory =
                Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize();

        Path demandeDirectory =
                baseDirectory
                        .resolve(
                                "demande-" + demande.getId()
                        )
                        .normalize();

        try {

            Files.createDirectories(
                    demandeDirectory
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Impossible de créer le dossier de stockage.",
                    e
            );
        }

        String baseName =
                originalFilename;

        int dotIndex =
                baseName.lastIndexOf('.');

        if (dotIndex > 0) {

            baseName =
                    baseName.substring(
                            0,
                            dotIndex
                    );
        }

        baseName =
                baseName.replaceAll(
                        "[^a-zA-Z0-9_-]",
                        "_"
                );

        String storedFilename =
                baseName
                        + "_"
                        + UUID.randomUUID()
                        + "."
                        + extension;

        Path targetPath =
                demandeDirectory
                        .resolve(storedFilename)
                        .normalize();

        if (!targetPath.startsWith(
                demandeDirectory
        )) {

            throw new RuntimeException(
                    "Chemin de fichier invalide."
            );
        }

        try {

            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erreur lors de l'enregistrement du fichier.",
                    e
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null
                || contentType.isBlank()) {

            contentType =
                    detectContentType(
                            targetPath
                    );
        }

        String cheminRelatif =
                "demande-"
                        + demande.getId()
                        + "/"
                        + storedFilename;

        Document document =
                Document.builder()
                        .nom(nom)
                        .type(type)
                        .cheminFichier(
                                cheminRelatif
                        )
                        .typeDocument(
                                typeDocument
                        )
                        .statutDocument(
                                StatutDocument.EN_ATTENTE
                        )
                        .tailleFichier(
                                file.getSize()
                        )
                        .contentType(
                                contentType
                        )
                        .demandeCredit(
                                demande
                        )
                        .build();

        Document savedDocument =
                documentRepository.save(
                        document
                );

        return DocumentMapper.toResponse(
                savedDocument
        );
    }

    // =========================================================
    // GET ALL ACCESSIBLES
    // =========================================================

    @Override
    public List<DocumentResponse> afficherTous() {

        Utilisateur utilisateurConnecte =
                getUtilisateurConnecte();

        return documentRepository
                .findAll()
                .stream()
                .filter(document ->
                        peutAccederDocument(
                                document,
                                utilisateurConnecte
                        )
                )
                .map(DocumentMapper::toResponse)
                .toList();
    }

    // =========================================================
    // GET PAR DEMANDE
    // =========================================================

    @Override
    public List<DocumentResponse> afficherParDemande(
            Long demandeCreditId
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        demandeCreditId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande de crédit introuvable."
                        )
                );

        // Vérification que le user connecté
        // a le droit de voir cette demande
        verifierAccesDemande(demande);

        return documentRepository
                .findByDemandeCreditId(
                        demandeCreditId
                )
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void supprimer(Long id) {

        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document introuvable."
                                )
                        );

        verifierAccesDocument(document);

        if (document.getCheminFichier() != null
                && !document.getCheminFichier().isBlank()) {

            try {

                Path baseDirectory =
                        Paths.get(uploadDir)
                                .toAbsolutePath()
                                .normalize();

                Path path =
                        baseDirectory
                                .resolve(
                                        document.getCheminFichier()
                                )
                                .normalize();

                if (path.startsWith(
                        baseDirectory
                )) {

                    Files.deleteIfExists(path);
                }

            } catch (IOException ignored) {
                // Fichier déjà absent
            }
        }

        documentRepository.delete(
                document
        );
    }

    // =========================================================
    // PREVIEW
    // =========================================================

    @Override
    public Resource preview(Long id) {

        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document introuvable."
                                )
                        );

        verifierAccesDocument(document);

        return loadResource(document);
    }

    // =========================================================
    // DOWNLOAD
    // =========================================================

    @Override
    public Resource download(Long id) {

        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document introuvable."
                                )
                        );

        verifierAccesDocument(document);

        return loadResource(document);
    }

    // =========================================================
    // VÉRIFICATION ACCÈS DEMANDE
    // =========================================================

    private void verifierAccesDemande(
            DemandeCredit demande
    ) {

        Utilisateur utilisateur =
                getUtilisateurConnecte();

        // =====================================================
        // ADMINISTRATEUR
        // =====================================================

        if (utilisateur instanceof Administrateur) {

            return;
        }

        // =====================================================
        // CLIENT
        // =====================================================

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

        // =====================================================
        // CONSEILLER
        // =====================================================

        if (utilisateur instanceof Conseiller conseiller) {

            if (demande.getConseiller() == null
                    || !demande.getConseiller()
                    .getId()
                    .equals(conseiller.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "est affectée à un autre conseiller."
                );
            }

            return;
        }

        // =====================================================
        // RESPONSABLE CREDIT
        // =====================================================

        if (utilisateur instanceof ResponsableCredit responsable) {

            if (demande.getResponsable() == null
                    || !demande.getResponsable()
                    .getId()
                    .equals(responsable.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande "
                                + "est affectée à un autre responsable."
                );
            }

            return;
        }

        // =====================================================
        // AUTRE ROLE
        // =====================================================

        throw new RuntimeException(
                "Accès interdit."
        );
    }

    // =========================================================
    // VÉRIFICATION ACCÈS DOCUMENT
    // =========================================================

    private void verifierAccesDocument(
            Document document
    ) {

        if (document == null
                || document.getDemandeCredit() == null) {

            throw new RuntimeException(
                    "Document invalide."
            );
        }

        verifierAccesDemande(
                document.getDemandeCredit()
        );
    }

    // =========================================================
    // FILTRAGE DOCUMENTS
    // =========================================================

    private boolean peutAccederDocument(
            Document document,
            Utilisateur utilisateur
    ) {

        if (document == null
                || document.getDemandeCredit() == null) {

            return false;
        }

        // =====================================================
        // ADMINISTRATEUR
        // =====================================================

        if (utilisateur instanceof Administrateur) {

            return true;
        }

        DemandeCredit demande =
                document.getDemandeCredit();

        // =====================================================
        // CLIENT
        // =====================================================

        if (utilisateur instanceof Client client) {

            return demande.getClient() != null
                    && demande.getClient()
                    .getId()
                    .equals(client.getId());
        }

        // =====================================================
        // CONSEILLER
        // =====================================================

        if (utilisateur instanceof Conseiller conseiller) {

            return demande.getConseiller() != null
                    && demande.getConseiller()
                    .getId()
                    .equals(conseiller.getId());
        }

        // =====================================================
        // RESPONSABLE CREDIT
        // =====================================================

        if (utilisateur instanceof ResponsableCredit responsable) {

            return demande.getResponsable() != null
                    && demande.getResponsable()
                    .getId()
                    .equals(responsable.getId());
        }

        return false;
    }

    // =========================================================
    // UTILISATEUR CONNECTÉ
    // =========================================================

    private Utilisateur getUtilisateurConnecte() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {

            throw new RuntimeException(
                    "Utilisateur non authentifié."
            );
        }

        String email =
                authentication.getName();

        return utilisateurRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable."
                        )
                );
    }

    // =========================================================
    // VALIDER DOCUMENT
    // =========================================================

    @Override
    public void validerDocument(Long id) {

        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document introuvable."
                                )
                        );

        verifierAccesDocument(document);

        document.setStatutDocument(
                StatutDocument.VALIDE
        );

        documentRepository.save(
                document
        );
    }

    // =========================================================
    // REFUSER DOCUMENT
    // =========================================================

    @Override
    public void refuserDocument(Long id) {

        Document document =
                documentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Document introuvable."
                                )
                        );

        verifierAccesDocument(document);

        document.setStatutDocument(
                StatutDocument.REFUSE
        );

        documentRepository.save(
                document
        );
    }

    // =========================================================
    // CHARGER FICHIER
    // =========================================================

    private Resource loadResource(
            Document document
    ) {

        if (document.getCheminFichier() == null
                || document.getCheminFichier().isBlank()) {

            throw new RuntimeException(
                    "Aucun fichier associé à ce document."
            );
        }

        try {

            Path baseDirectory =
                    Paths.get(uploadDir)
                            .toAbsolutePath()
                            .normalize();

            String chemin =
                    document.getCheminFichier()
                            .replace("\\", "/")
                            .trim();

            while (chemin.startsWith("./")) {

                chemin =
                        chemin.substring(2);
            }

            while (chemin.startsWith("/")) {

                chemin =
                        chemin.substring(1);
            }

            if (chemin.startsWith(
                    "uploads/documents/"
            )) {

                chemin =
                        chemin.substring(
                                "uploads/documents/"
                                        .length()
                        );
            }

            if (chemin.startsWith(
                    "documents/"
            )) {

                chemin =
                        chemin.substring(
                                "documents/"
                                        .length()
                        );
            }

            Path path =
                    baseDirectory
                            .resolve(chemin)
                            .normalize();

            if (!path.startsWith(
                    baseDirectory
            )) {

                throw new RuntimeException(
                        "Chemin de fichier invalide."
                );
            }

            if (!Files.exists(path)
                    || !Files.isReadable(path)) {

                throw new RuntimeException(
                        "Fichier physique introuvable : "
                                + path.toAbsolutePath()
                );
            }

            Resource resource =
                    new UrlResource(
                            path.toUri()
                    );

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new RuntimeException(
                        "Fichier physique introuvable : "
                                + path.toAbsolutePath()
                );
            }

            return resource;

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Chemin du fichier invalide.",
                    e
            );
        }
    }

    // =========================================================
    // EXTENSION
    // =========================================================

    private String getExtension(
            String filename
    ) {

        int dotIndex =
                filename.lastIndexOf('.');

        if (dotIndex == -1
                || dotIndex == filename.length() - 1) {

            throw new RuntimeException(
                    "Le fichier doit posséder une extension."
            );
        }

        return filename
                .substring(
                        dotIndex + 1
                )
                .toLowerCase();
    }

    // =========================================================
    // CONTENT TYPE
    // =========================================================

    private String detectContentType(
            Path path
    ) {

        try {

            String detected =
                    Files.probeContentType(
                            path
                    );

            if (detected != null) {

                return detected;
            }

        } catch (IOException ignored) {
        }

        return "application/octet-stream";
    }
}