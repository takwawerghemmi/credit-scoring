package com.creditscoring.service;

import com.creditscoring.dto.request.DemandeCreditRequest;
import com.creditscoring.dto.reponse.DemandeCreditResponse;

import java.util.List;

public interface DemandeCreditService {

    DemandeCreditResponse creer(
            DemandeCreditRequest request,
            String emailUtilisateur
    );

    DemandeCreditResponse modifier(
            Long id,
            DemandeCreditRequest request
    );

    DemandeCreditResponse modifierPourUtilisateur(
            Long id,
            DemandeCreditRequest request,
            String emailUtilisateur,
            String role
    );

    DemandeCreditResponse trouverParId(
            Long id
    );

    DemandeCreditResponse trouverParIdPourUtilisateur(
            Long id,
            String emailUtilisateur,
            String role
    );

    List<DemandeCreditResponse> afficherToutes();

    List<DemandeCreditResponse> afficherMesDemandes(
            String emailUtilisateur
    );

    List<DemandeCreditResponse> afficherParClient(
            Long clientId
    );

    void supprimerPourUtilisateur(
            Long id,
            String emailUtilisateur,
            String role
    );

    void supprimer(
            Long id
    );
    List<DemandeCreditResponse> afficherParConseiller(
            String emailUtilisateur
    );
}