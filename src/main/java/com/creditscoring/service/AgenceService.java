package com.creditscoring.service;

import com.creditscoring.dto.request.AgenceRequest;
import com.creditscoring.dto.reponse.AgenceResponse;

import java.util.List;

public interface AgenceService {

    AgenceResponse creerAgence(AgenceRequest request);

    List<AgenceResponse> obtenirToutesLesAgences();

    AgenceResponse obtenirAgence(Long id);

    AgenceResponse modifierAgence(Long id, AgenceRequest request);

    void supprimerAgence(Long id);
}