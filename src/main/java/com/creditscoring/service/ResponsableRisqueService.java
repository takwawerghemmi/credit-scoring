package com.creditscoring.service;

import com.creditscoring.dto.reponse.ResponsableRisqueResponse;

import java.util.List;

public interface ResponsableRisqueService {

    List<ResponsableRisqueResponse> getDossiersARisque(
            String emailUtilisateur
    );

    List<ResponsableRisqueResponse> getDossiersPrioritaires(
            String emailUtilisateur
    );
}