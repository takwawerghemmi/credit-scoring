package com.creditscoring.service;

import com.creditscoring.dto.request.ResponsableValidationRequest;

public interface DoubleValidationService {

    String premiereValidation(
            Long demandeId,
            String emailValidateur,
            String role
    );


    String validationResponsable(
            Long demandeId,
            ResponsableValidationRequest request,
            String emailValidateur
    );
}