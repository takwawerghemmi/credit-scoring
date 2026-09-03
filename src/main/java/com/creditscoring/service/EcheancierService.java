package com.creditscoring.service;

import com.creditscoring.dto.reponse.AmortissementResponse;
import com.creditscoring.dto.reponse.EcheanceResponse;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;

import java.util.List;

public interface EcheancierService {

    // =====================================================
    // CALCUL THÉORIQUE DE L'AMORTISSEMENT
    // =====================================================

    List<AmortissementResponse> genererEcheancier(
            DemandeCredit demandeCredit,
            String email
    );

    // =====================================================
    // CRÉER LES ÉCHÉANCES RÉELLES
    // =====================================================

    List<EcheanceResponse> creerEcheances(
            Contrat contrat
    );

    // =====================================================
    // RÉCUPÉRER LES ÉCHÉANCES D'UN CONTRAT
    // =====================================================

    List<EcheanceResponse> getEcheancesByContrat(
            Long contratId,
            String email
    );
}