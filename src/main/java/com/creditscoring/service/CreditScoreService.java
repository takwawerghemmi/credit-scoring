package com.creditscoring.service;

import com.creditscoring.dto.request.CreditScoreRequest;
import com.creditscoring.dto.reponse.CreditScoreResponse;

import java.util.List;

public interface CreditScoreService {

    CreditScoreResponse calculerScore(
            CreditScoreRequest request,
            String emailUtilisateur
    );

    List<CreditScoreResponse> getAllScores(
            String emailUtilisateur
    );

    CreditScoreResponse getScore(
            Long id,
            String emailUtilisateur
    );
}