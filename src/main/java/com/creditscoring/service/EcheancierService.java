package com.creditscoring.service;

import com.creditscoring.dto.reponse.AmortissementResponse;
import com.creditscoring.dto.reponse.EcheanceResponse;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;

import java.util.List;

public interface EcheancierService {

    // Calcul théorique de l'amortissement
    List<AmortissementResponse> genererEcheancier(
            DemandeCredit demandeCredit
    );

    // Créer les échéances réelles dans la base
    List<EcheanceResponse> creerEcheances(
            Contrat contrat
    );

    // Récupérer les échéances d'un contrat
    List<EcheanceResponse> getEcheancesByContrat(
            Long contratId
    );
}