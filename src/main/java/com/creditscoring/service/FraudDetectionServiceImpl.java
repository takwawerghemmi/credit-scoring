package com.creditscoring.service;

import com.creditscoring.auth.entity.RefreshToken;
import com.creditscoring.dto.reponse.FraudDetectionResponse;
import com.creditscoring.entity.DemandeCredit;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {
    @Override
    public FraudDetectionResponse detecterFraude(DemandeCredit demande) {

        int score = 0;

        if (demande.getMontant() != null && demande.getMontant() > 200000) {
            score += 40;
        }

        if (demande.getRevenuMensuel() != null &&
                demande.getMontant() != null &&
                demande.getMontant() > demande.getRevenuMensuel() * 30) {
            score += 30;
        }

        if (demande.getChargesMensuelles() != null &&
                demande.getRevenuMensuel() != null &&
                demande.getChargesMensuelles() > demande.getRevenuMensuel() * 0.7) {
            score += 20;
        }

        if (demande.getDuree() != null && demande.getDuree() < 12) {
            score += 10;
        }

        if (score >= 70) {
            return new FraudDetectionResponse(
                    true,
                    "ÉLEVÉ",
                    "Fraude fortement suspectée"
            );
        }

        if (score >= 40) {
            return new FraudDetectionResponse(
                    false,
                    "MOYEN",
                    "Vérification complémentaire recommandée"
            );
        }

        return new FraudDetectionResponse(
                false,
                "FAIBLE",
                "Aucune anomalie détectée"
        );
    }
}