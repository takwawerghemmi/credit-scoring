package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Document;
import com.creditscoring.enums.StatutDocument;
import com.creditscoring.enums.TypeDocument;
import com.creditscoring.mapper.DocumentMapper;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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

    @Value("${app.document.upload-dir:./uploads/documents}")
    private String uploadDir;


    // =========================================================
    // UPLOAD REEL DU FICHIER
    // =========================================================
    @Override
    public DocumentResponse ajouterFichier(
            MultipartFile file,
            String nom,
            String type,
            Long demandeCreditId
    ) {

        // -----------------------------------------------------
        // 1. Vérifier le fichier
        // -----------------------------------------------------
        if (file == null || file.isEmpty()) {
            throw new RuntimeException(
                    "Veuillez sélectionner un fichier."
            );
        }


        // -----------------------------------------------------
        // 2. Vérifier la demande de crédit
        // -----------------------------------------------------
        DemandeCredit demande = demandeRepository
                .findById(demandeCreditId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Demande de crédit introuvable."
                        )
                );


        // -----------------------------------------------------
        // 3. Vérifier la taille maximale : 10 MB
        // -----------------------------------------------------
        long maxSize = 10 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new RuntimeException(
                    "Le fichier ne doit pas dépasser 10 MB."
            );
        }


        // -----------------------------------------------------
        // 4. Nom original
        // -----------------------------------------------------
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.trim().isEmpty()) {

            throw new RuntimeException(
                    "Nom de fichier invalide."
            );
        }


        // -----------------------------------------------------
        // 5. Nettoyer le nom
        // -----------------------------------------------------
        originalFilename = Paths
                .get(originalFilename)
                .getFileName()
                .toString();


        // -----------------------------------------------------
        // 6. Vérifier extension
        // -----------------------------------------------------
        String extension = getExtension(originalFilename);

        if (!extension.equals("pdf")
                && !extension.equals("png")
                && !extension.equals("jpg")
                && !extension.equals("jpeg")) {

            throw new RuntimeException(
                    "Format non autorisé. " +
                            "Seuls PDF, PNG, JPG et JPEG sont acceptés."
            );
        }


        // -----------------------------------------------------
        // 7. Vérifier le type du document
        // -----------------------------------------------------
        TypeDocument typeDocument;

        try {

            typeDocument = TypeDocument.valueOf(
                    type.trim().toUpperCase()
            );

        } catch (IllegalArgumentException e) {

            throw new RuntimeException(
                    "Type de document invalide : " + type
            );
        }


        // -----------------------------------------------------
        // 8. Vérifier doublon
        // -----------------------------------------------------
        boolean existe = documentRepository
                .existsByNomAndDemandeCreditId(
                        nom,
                        demande.getId()
                );

        if (existe) {

            throw new RuntimeException(
                    "Ce document existe déjà pour cette demande."
            );
        }


        // -----------------------------------------------------
        // 9. Dossier principal
        //
        // Exemple :
        // /home/takwa/credit-scoring/uploads/documents
        // -----------------------------------------------------
        Path baseDirectory = Paths
                .get(uploadDir)
                .toAbsolutePath()
                .normalize();

        // -----------------------------------------------------
        // 10. Dossier de la demande
        //
        // Exemple :
        // uploads/documents/demande-2
        // -----------------------------------------------------
        Path demandeDirectory = baseDirectory
                .resolve("demande-" + demande.getId())
                .normalize();

        try {

            Files.createDirectories(demandeDirectory);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Impossible de créer le dossier de stockage.",
                    e
            );
        }


        // -----------------------------------------------------
        // 11. Générer un nom sécurisé et unique
        // -----------------------------------------------------
        String baseName = originalFilename;

        int dotIndex = baseName.lastIndexOf('.');

        if (dotIndex > 0) {
            baseName = baseName.substring(0, dotIndex);
        }

        baseName = baseName
                .replaceAll("[^a-zA-Z0-9_-]", "_");

        String storedFilename =
                baseName
                        + "_"
                        + UUID.randomUUID()
                        + "."
                        + extension;


        // -----------------------------------------------------
        // 12. Chemin physique du fichier
        // -----------------------------------------------------
        Path targetPath = demandeDirectory
                .resolve(storedFilename)
                .normalize();


        // Sécurité
        if (!targetPath.startsWith(demandeDirectory)) {

            throw new RuntimeException(
                    "Chemin de fichier invalide."
            );
        }


        // -----------------------------------------------------
        // 13. UPLOAD REEL
        // -----------------------------------------------------
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


        // -----------------------------------------------------
        // 14. Content-Type
        // -----------------------------------------------------
        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = detectContentType(targetPath);
        }


        // -----------------------------------------------------
        // 15. IMPORTANT :
        // chemin relatif enregistré dans la DB
        //
        // Exemple :
        // demande-2/CIN_Yassine_xxx.pdf
        //
        // PAS :
        // uploads/documents/demande-2/...
        // -----------------------------------------------------
        String cheminRelatif =
                "demande-"
                        + demande.getId()
                        + "/"
                        + storedFilename;


        // -----------------------------------------------------
        // 16. Créer Document
        // -----------------------------------------------------
        Document document = Document.builder()
                .nom(nom)
                .type(type)
                .cheminFichier(cheminRelatif)
                .typeDocument(typeDocument)
                .statutDocument(StatutDocument.EN_ATTENTE)
                .tailleFichier(file.getSize())
                .contentType(contentType)
                .demandeCredit(demande)
                .build();


        // -----------------------------------------------------
        // 17. Sauvegarder dans DB
        // -----------------------------------------------------
        Document savedDocument =
                documentRepository.save(document);


        // -----------------------------------------------------
        // 18. Réponse
        // -----------------------------------------------------
        return DocumentMapper.toResponse(savedDocument);
    }


    // =========================================================
    // GET ALL DOCUMENTS
    // =========================================================
    @Override
    public List<DocumentResponse> afficherTous() {

        return documentRepository
                .findAll()
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }


    // =========================================================
    // DELETE DOCUMENT
    // =========================================================
    @Override
    public void supprimer(Long id) {

        Document document = documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Document introuvable."
                        )
                );


        // -----------------------------------------------------
        // Supprimer aussi le fichier physique
        // -----------------------------------------------------
        if (document.getCheminFichier() != null
                && !document.getCheminFichier().isBlank()) {

            try {

                Path baseDirectory = Paths
                        .get(uploadDir)
                        .toAbsolutePath()
                        .normalize();

                Path path = baseDirectory
                        .resolve(document.getCheminFichier())
                        .normalize();

                if (path.startsWith(baseDirectory)) {
                    Files.deleteIfExists(path);
                }

            } catch (IOException ignored) {
                // Le fichier peut déjà ne plus exister.
            }
        }


        // -----------------------------------------------------
        // Supprimer de la DB
        // -----------------------------------------------------
        documentRepository.delete(document);
    }


    // =========================================================
    // PREVIEW
    // =========================================================
    @Override
    public Resource preview(Long id) {

        Document document = documentRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Document introuvable."
                        )
                );

        return loadResource(document);
    }


    // =========================================================
    // DOWNLOAD
    // =========================================================
    @Override
    public Resource download(Long id) {

        return preview(id);
    }


    // =========================================================
    // CHARGER LE FICHIER PHYSIQUE
    // =========================================================
    private Resource loadResource(Document document) {

        if (document.getCheminFichier() == null
                || document.getCheminFichier().isBlank()) {

            throw new RuntimeException(
                    "Aucun fichier associé à ce document."
            );
        }


        try {

            // -------------------------------------------------
            // Dossier principal
            // -------------------------------------------------
            Path baseDirectory = Paths
                    .get(uploadDir)
                    .toAbsolutePath()
                    .normalize();


            // -------------------------------------------------
            // Nettoyer les anciens chemins éventuels
            //
            // Exemple ancien :
            // documents/client9/cin_yassine.pdf
            //
            // On retire "uploads/documents/" ou "./"
            // si nécessaire.
            // -------------------------------------------------
            String chemin = document
                    .getCheminFichier()
                    .replace("\\", "/")
                    .trim();

            while (chemin.startsWith("./")) {
                chemin = chemin.substring(2);
            }

            while (chemin.startsWith("/")) {
                chemin = chemin.substring(1);
            }

            // Si ancien chemin contient "uploads/documents/"
            if (chemin.startsWith("uploads/documents/")) {

                chemin = chemin.substring(
                        "uploads/documents/".length()
                );
            }

            // Si ancien chemin commence par documents/
            if (chemin.startsWith("documents/")) {

                chemin = chemin.substring(
                        "documents/".length()
                );
            }


            // -------------------------------------------------
            // Construire chemin final
            // -------------------------------------------------
            Path path = baseDirectory
                    .resolve(chemin)
                    .normalize();


            // -------------------------------------------------
            // Sécurité
            // -------------------------------------------------
            if (!path.startsWith(baseDirectory)) {

                throw new RuntimeException(
                        "Chemin de fichier invalide."
                );
            }


            // -------------------------------------------------
            // DEBUG
            // -------------------------------------------------
            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "Upload directory : "
                            + baseDirectory
            );

            System.out.println(
                    "Chemin DB : "
                            + document.getCheminFichier()
            );

            System.out.println(
                    "Chemin nettoyé : "
                            + chemin
            );

            System.out.println(
                    "Fichier recherché : "
                            + path.toAbsolutePath()
            );

            System.out.println(
                    "Fichier existe : "
                            + Files.exists(path)
            );

            System.out.println(
                    "Fichier lisible : "
                            + Files.isReadable(path)
            );

            System.out.println(
                    "========================================"
            );


            // -------------------------------------------------
            // Vérifier existence
            // -------------------------------------------------
            if (!Files.exists(path)
                    || !Files.isReadable(path)) {

                throw new RuntimeException(
                        "Fichier physique introuvable : "
                                + path.toAbsolutePath()
                );
            }


            // -------------------------------------------------
            // Créer Resource
            // -------------------------------------------------
            Resource resource =
                    new UrlResource(path.toUri());


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
    // GET EXTENSION
    // =========================================================
    private String getExtension(String filename) {

        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex == -1
                || dotIndex == filename.length() - 1) {

            throw new RuntimeException(
                    "Le fichier doit posséder une extension."
            );
        }

        return filename
                .substring(dotIndex + 1)
                .toLowerCase();
    }


    // =========================================================
    // DETECT CONTENT TYPE
    // =========================================================
    private String detectContentType(Path path) {

        try {

            String detected =
                    Files.probeContentType(path);

            if (detected != null) {
                return detected;
            }

        } catch (IOException ignored) {
        }

        return "application/octet-stream";
    }
}