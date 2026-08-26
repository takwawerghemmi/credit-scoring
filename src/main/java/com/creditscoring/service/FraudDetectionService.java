package com.creditscoring.service;

import com.creditscoring.dto.reponse.FraudDetectionResponse;
import com.creditscoring.entity.DemandeCredit;

public interface FraudDetectionService {

    FraudDetectionResponse detecterFraude(DemandeCredit demande);

}