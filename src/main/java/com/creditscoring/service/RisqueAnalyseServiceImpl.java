package com.creditscoring.service;

import com.creditscoring.entity.Client;
import com.creditscoring.entity.DemandeCredit;
import org.springframework.stereotype.Service;

@Service
public class RisqueAnalyseServiceImpl
        implements RisqueAnalyseService {

    @Override
    public String analyserRisque(
            DemandeCredit demande
    ) {

        if (demande == null
                || demande.getClient() == null) {

            return "RISQUE ELEVE";
        }

        Client client = demande.getClient();

        double revenu =
                client.getRevenuMensuel() != null
                        ? client.getRevenuMensuel()
                        : 0.0;

        double dettes =
                client.getDettesExistantes() != null
                        ? client.getDettesExistantes()
                        : 0.0;

        double montant =
                demande.getMontant() != null
                        ? demande.getMontant()
                        : 0.0;

        double charges =
                demande.getChargesMensuelles() != null
                        ? demande.getChargesMensuelles()
                        : 0.0;

        // Aucun revenu = risque élevé
        if (revenu <= 0) {
            return "RISQUE ELEVE";
        }

        // ---------------------------------------------------------
        // Ratios
        // ---------------------------------------------------------

        double tauxDettes =
                dettes / revenu;

        double tauxCharges =
                charges / revenu;

        double tauxMontant =
                montant / revenu;

        double ratioGlobal =
                (dettes + montant) / revenu;

        // ---------------------------------------------------------
        // RISQUE FAIBLE
        //
        // Profil financier sain :
        // - très peu de dettes
        // - charges faibles
        // - montant demandé raisonnable
        // ---------------------------------------------------------

        if (tauxDettes <= 0.10
                && tauxCharges <= 0.10
                && tauxMontant <= 1.00) {

            return "RISQUE FAIBLE";
        }

        // ---------------------------------------------------------
        // RISQUE MOYEN
        //
        // Situation acceptable mais nécessitant une analyse.
        // ---------------------------------------------------------

        if (ratioGlobal <= 0.60
                && tauxDettes <= 0.30
                && tauxCharges <= 0.50) {

            return "RISQUE MOYEN";
        }

        // ---------------------------------------------------------
        // RISQUE ÉLEVÉ
        // ---------------------------------------------------------

        return "RISQUE ELEVE";
    }
}