package com.creditscoring.service;

import com.creditscoring.dto.reponse.DocumentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse ajouterFichier(
            MultipartFile file,
            String nom,
            String type,
            Long demandeCreditId
    );

    List<DocumentResponse> afficherTous();

    void supprimer(Long id);

    Resource preview(Long id);

    Resource download(Long id);
}