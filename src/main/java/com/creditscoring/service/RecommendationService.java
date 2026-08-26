package com.creditscoring.service;

import com.creditscoring.dto.reponse.RecommendationResponse;
import com.creditscoring.entity.DemandeCredit;

public interface RecommendationService {

    RecommendationResponse genererRecommandations(DemandeCredit demande);

}