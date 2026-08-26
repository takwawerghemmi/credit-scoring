package com.creditscoring.dto.reponse;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private Long totalCredits;
    private Long creditsApprouves;
    private Long creditsRefuses;
    private Long creditsEnCours;
    private Double montantTotalAccorde;
    private Double tauxApprobation;
    private Double tauxRemboursement;
    private Long clientsARisque;
    private Map<String, Long> creditsParMois;
    private Map<String, Long> creditsParAgence;
    private Map<String, Long> creditsParType;
    private List<RisqueClientResume> topClientsRisque;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RisqueClientResume {
        private Long clientId;
        private String nomComplet;
        private Double scoreConfiance;
        private String niveauRisque;
        private Integer echeancesEnRetard;
        private Double tauxAcceptation;
        private Double tauxRefus;
        private Double montantMoyenAccorde;
        private Long nombreAlertesFraude;
    }
}
