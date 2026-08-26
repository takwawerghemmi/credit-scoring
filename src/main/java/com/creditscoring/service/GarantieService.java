package com.creditscoring.service;

import com.creditscoring.dto.request.GarantieRequest;
import com.creditscoring.dto.reponse.GarantieResponse;

import java.util.List;

public interface GarantieService {

    GarantieResponse ajouter(GarantieRequest request);

    List<GarantieResponse> afficherToutes();

    List<GarantieResponse> afficherParDemande(
            Long demandeId
    );

    void supprimer(Long id);
}