package com.creditscoring.service;

import com.creditscoring.entity.Client;
import com.creditscoring.entity.DemandeCredit;
import org.springframework.stereotype.Service;

@Service
public class RisqueAnalyseServiceImpl implements RisqueAnalyseService {

    @Override
    public String analyserRisque(DemandeCredit demande) {

        Client client = demande.getClient();

        double revenu = client.getRevenuMensuel() != null
                ? client.getRevenuMensuel()
                : 0.0;

        double dettes = client.getDettesExistantes() != null
                ? client.getDettesExistantes()
                : 0.0;
        double montant = demande.getMontant();

        if (revenu <= 0) {
            return "RISQUE ELEVE";
        }

        double ratio = (dettes + montant) / revenu;

        if (ratio < 0.30) {
            return "RISQUE FAIBLE";
        } else if (ratio < 0.60) {
            return "RISQUE MOYEN";
        } else {
            return "RISQUE ELEVE";
        }
    }
}