package com.creditscoring.service;

import com.creditscoring.dto.reponse.RecommendationResponse;
import com.creditscoring.entity.DemandeCredit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    @Override
    public RecommendationResponse genererRecommandations(DemandeCredit demande) {

        List<String> recommandations = new ArrayList<>();

        String decision = "ACCORD POSSIBLE";

        if (demande.getRevenuMensuel() != null &&
                demande.getRevenuMensuel() < 2500) {

            recommandations.add("Revenu mensuel faible : vérifier la capacité de remboursement.");
            decision = "ANALYSE COMPLÉMENTAIRE";
        }

        if (demande.getChargesMensuelles() != null &&
                demande.getRevenuMensuel() != null &&
                demande.getChargesMensuelles() > demande.getRevenuMensuel() * 0.6) {

            recommandations.add("Charges mensuelles élevées.");
            decision = "ANALYSE COMPLÉMENTAIRE";
        }

        if (demande.getMontant() != null &&
                demande.getMontant() > 150000) {

            recommandations.add("Montant élevé : demander des garanties supplémentaires.");
        }

        if (demande.getDuree() != null &&
                demande.getDuree() > 120) {

            recommandations.add("Durée très longue : revoir les conditions du prêt.");
        }

        if (recommandations.isEmpty()) {

            recommandations.add("Profil favorable.");
            recommandations.add("Accord recommandé.");
            recommandations.add("Suivi normal.");
        }

        return new RecommendationResponse(
                decision,
                recommandations
        );
    }
}