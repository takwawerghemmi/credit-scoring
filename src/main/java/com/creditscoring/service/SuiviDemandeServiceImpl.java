package com.creditscoring.service;

import com.creditscoring.entity.DemandeCredit;
import org.springframework.stereotype.Service;

@Service
public class SuiviDemandeServiceImpl implements SuiviDemandeService {

    @Override
    public String suivreDemande(DemandeCredit demandeCredit) {

        if (demandeCredit == null) {
            return "Demande introuvable";
        }

        return "Statut actuel : " + demandeCredit.getStatut();
    }
}