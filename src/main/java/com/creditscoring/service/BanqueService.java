package com.creditscoring.service;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.reponse.BanqueResponse;

import java.util.List;

public interface BanqueService {

    BanqueResponse ajouter(BanqueRequest request);

    BanqueResponse modifier(Long id, BanqueRequest request);

    BanqueResponse trouverParId(Long id);

    List<BanqueResponse> afficherToutes();

    void supprimer(Long id);
}