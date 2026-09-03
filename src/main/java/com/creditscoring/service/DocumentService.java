package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    // =========================================================
    // UPLOAD
    // =========================================================

    DocumentResponse ajouterFichier(
            MultipartFile file,
            String nom,
            String type,
            Long demandeCreditId
    );

    // =========================================================
    // GET ALL DOCUMENTS ACCESSIBLES
    // =========================================================

    List<DocumentResponse> afficherTous();

    void supprimer(Long id);

    Resource preview(Long id);

    Resource download(Long id);

    void validerDocument(Long id);

    void refuserDocument(Long id);


    // =========================================================
    // GET DOCUMENTS D'UNE DEMANDE
    // =========================================================

    List<DocumentResponse> afficherParDemande(
            Long demandeCreditId
    );




}