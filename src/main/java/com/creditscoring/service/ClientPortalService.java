package com.creditscoring.service;

import com.creditscoring.dto.reponse.ClientDashboardDTO;

public interface ClientPortalService {

    ClientDashboardDTO getDashboard(String email);
}
