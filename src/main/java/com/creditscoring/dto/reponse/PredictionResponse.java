package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PredictionResponse {

    private String prediction;

    private double score;

    private double probabilite;
}