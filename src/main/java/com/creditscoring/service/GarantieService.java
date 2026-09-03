package com.creditscoring.service;

import com.creditscoring.dto.request.GarantieRequest;
import com.creditscoring.dto.reponse.GarantieResponse;

import java.util.List;

public interface GarantieService {

    GarantieResponse ajouter(
            GarantieRequest request,
            String emailUtilisateur,
            String roleUtilisateur
    );

    List<GarantieResponse> afficherToutes(
            String emailUtilisateur,
            String roleUtilisateur
    );

    List<GarantieResponse> afficherParDemande(
            Long demandeId,
            String emailUtilisateur,
            String roleUtilisateur
    );

    void supprimer(
            Long id,
            String emailUtilisateur,
            String roleUtilisateur
    );
}