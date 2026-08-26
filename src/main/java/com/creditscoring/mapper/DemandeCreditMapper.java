package com.creditscoring.mapper;

import com.creditscoring.dto.reponse.DemandeCreditResponse;
import com.creditscoring.entity.DemandeCredit;

public class DemandeCreditMapper {

    private DemandeCreditMapper() {
    }

    public static DemandeCreditResponse toResponse(DemandeCredit demande) {

        return DemandeCreditResponse.builder()
                .id(demande.getId())
                .montant(demande.getMontant())
                .duree(demande.getDuree())
                .typeCredit(demande.getTypeCredit())
                .statut(demande.getStatut() != null ? demande.getStatut().toString() : null)                .revenuMensuel(demande.getRevenuMensuel())
                .chargesMensuelles(demande.getChargesMensuelles())
                .dateDemande(demande.getDateDemande())
                .clientId(demande.getClient() != null ? demande.getClient().getId() : null)
                .conseillerId(demande.getConseiller() != null ? demande.getConseiller().getId() : null)
                .banqueId(demande.getBanque() != null ? demande.getBanque().getId() : null)
                .build();
    }

}