package com.creditscoring.service;

import com.creditscoring.dto.reponse.ResponsableDashboardResponse;

public interface ResponsableDashboardService {

    ResponsableDashboardResponse getDashboard();

    ResponsableDashboardResponse.DossierResponsable getDossier(Long demandeId);
}