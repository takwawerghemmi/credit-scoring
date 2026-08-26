package com.creditscoring.service;

import com.creditscoring.dto.request.DemandeCreditRequest;
import com.creditscoring.dto.reponse.DemandeCreditResponse;

import java.util.List;

public interface DemandeCreditService {

    DemandeCreditResponse creer(DemandeCreditRequest request, String emailUtilisateur);

    DemandeCreditResponse modifier(Long id, DemandeCreditRequest request);

    DemandeCreditResponse modifierPourUtilisateur(
            Long id,
            DemandeCreditRequest request,
            String emailUtilisateur,
            String role
    );

    DemandeCreditResponse trouverParId(Long id);

    DemandeCreditResponse trouverParIdPourUtilisateur(
            Long id,
            String emailUtilisateur,
            String role
    );

    List<DemandeCreditResponse> afficherToutes();

    List<DemandeCreditResponse> afficherParClient(Long clientId);

    List<DemandeCreditResponse> afficherMesDemandes(String emailUtilisateur);

    void supprimer(Long id);

    void supprimerPourUtilisateur(
            Long id,
            String emailUtilisateur,
            String role
    );
}