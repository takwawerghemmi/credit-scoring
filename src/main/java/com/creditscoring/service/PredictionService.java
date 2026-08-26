package com.creditscoring.service;

import com.creditscoring.dto.reponse.PredictionResponse;
import com.creditscoring.entity.DemandeCredit;

public interface PredictionService {

    PredictionResponse predire(DemandeCredit demande);

}