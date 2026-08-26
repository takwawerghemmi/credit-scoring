package com.creditscoring.service;

import com.creditscoring.dto.request.DecisionRequest;
import com.creditscoring.dto.reponse.DecisionResponse;

public interface DecisionCreditService {

    DecisionResponse prendreDecision(
            DecisionRequest request,
            String role,
            String emailDirecteur
    );
}