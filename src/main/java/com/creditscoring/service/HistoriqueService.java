package com.creditscoring.service;

import com.creditscoring.dto.reponse.HistoriqueResponse;
import java.util.List;

public interface HistoriqueService {

    List<HistoriqueResponse> getAllHistoriques();

    HistoriqueResponse getHistoriqueById(Long id);

    List<HistoriqueResponse> getHistoriqueUtilisateur(Long utilisateurId);

    void supprimerHistorique(Long id);
}