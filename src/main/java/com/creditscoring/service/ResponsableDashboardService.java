package com.creditscoring.service;

import com.creditscoring.dto.reponse.ResponsableDashboardResponse;

public interface ResponsableDashboardService {

    ResponsableDashboardResponse getDashboard(String email);

    ResponsableDashboardResponse.DossierResponsable getDossier(
            Long demandeId,
            String email
    );
}
