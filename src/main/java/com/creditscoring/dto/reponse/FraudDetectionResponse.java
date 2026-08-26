package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FraudDetectionResponse {

    private boolean fraude;

    private String niveauRisque;

    private String message;

}