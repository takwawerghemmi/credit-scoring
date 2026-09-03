package com.creditscoring.service;

import com.creditscoring.dto.reponse.PredictionResponse;
import com.creditscoring.entity.DemandeCredit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final MlScoringClient mlScoringClient;

    @Override
    public PredictionResponse predire(
            DemandeCredit demande
    ) {

        Map<String, Object> result =
                mlScoringClient.predict(demande);

        double probabilityDefault =
                toDouble(
                        result.get("probability_default")
                );

        double creditScore =
                toDouble(
                        result.get("credit_score")
                );

        String riskLevel =
                String.valueOf(
                        result.get("risk_level")
                );

        String decision =
                probabilityDefault < 0.50
                        ? "APPROBATION PROBABLE"
                        : "RISQUE DE REFUS";

        return new PredictionResponse(
                decision,
                creditScore,
                probabilityDefault
        );
    }

    private double toDouble(Object value) {

        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return Double.parseDouble(
                value.toString()
        );
    }
}
