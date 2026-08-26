package com.creditscoring.service;

import com.creditscoring.dto.request.PaiementRequest;
import com.creditscoring.dto.reponse.PaiementResponse;

import java.util.List;

public interface PaiementService {

    PaiementResponse creerPaiement(
            PaiementRequest request
    );

    PaiementResponse confirmerPaiement(
            Long paiementId
    );

    PaiementResponse echouerPaiement(
            Long paiementId
    );

    List<PaiementResponse> getAllPaiements();

    PaiementResponse getPaiementById(
            Long id
    );

    void deletePaiement(
            Long id
    );

    List<PaiementResponse> getPaiementsByContrat(
            Long contratId
    );
}