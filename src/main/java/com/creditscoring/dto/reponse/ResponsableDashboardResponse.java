package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsableDashboardResponse {

    private Kpi kpi;

    private List<DossierResponsable> dossiersAControler;

    private List<DossierResponsable> derniersDossiers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Kpi {

        private long totalDemandes;

        private long dossiersAControler;

        private long premieresValidations;

        private long deuxiemesValidations;

        private long dossiersApprouves;

        private long dossiersRefuses;

        private long risqueFaible;

        private long risqueMoyen;

        private long risqueEleve;

        private long anomalies;

        private double scoreMoyen;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DossierResponsable {

        private Long demandeId;

        private String clientNom;

        private String clientPrenom;

        private Double montant;

        private Integer duree;

        private String typeCredit;

        private String statut;

        private Double score;

        private String niveauRisque;

        private boolean premiereValidation;

        private boolean deuxiemeValidation;

        private boolean pretPourValidation;

        private LocalDate dateDemande;
    }
}