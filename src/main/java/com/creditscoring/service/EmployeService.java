package com.creditscoring.service;

import com.creditscoring.dto.request.CreateEmployeRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;

public interface EmployeService {

    UtilisateurResponse creerAdministrateur(CreateEmployeRequest request);

    UtilisateurResponse creerConseiller(CreateEmployeRequest request);

    UtilisateurResponse creerDirecteur(CreateEmployeRequest request);

    UtilisateurResponse creerResponsableCredit(CreateEmployeRequest request);

}
