package com.creditscoring.service;

import com.creditscoring.dto.request.ContratRequest;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;

import java.util.List;

public interface ContratService {

    Contrat creerContrat(
            ContratRequest request
    );

    Contrat creerContratAutomatiquement(
            DemandeCredit demande,
            Utilisateur directeur
    );

    List<Contrat> getAllContrats();

    Contrat getContratById(
            Long id
    );

    Contrat getContratByDemandeId(
            Long demandeId
    );

    Contrat updateContrat(
            Long id,
            ContratRequest request
    );

    void deleteContrat(
            Long id
    );
}