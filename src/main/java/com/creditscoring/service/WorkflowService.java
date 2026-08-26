package com.creditscoring.service;

import com.creditscoring.dto.request.WorkflowTransitionRequest;
import com.creditscoring.dto.reponse.SuiviDemandeResponse;
import com.creditscoring.enums.StatutDemande;

public interface WorkflowService {

    void transitionner(
            Long demandeId,
            WorkflowTransitionRequest request,
            String emailUtilisateur,
            String roleUtilisateur
    );

    StatutDemande getStatutActuel(Long demandeId);

    SuiviDemandeResponse getSuivi(Long demandeId);
}