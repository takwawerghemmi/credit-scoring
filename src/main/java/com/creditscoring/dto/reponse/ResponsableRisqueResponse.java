package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsableRisqueResponse {

    private Long demandeId;

    private String clientNom;

    private String clientPrenom;

    private Double montant;

    private Integer duree;

    private String typeCredit;

    private String statut;

    private Double score;

    private String niveauRisque;

    private String priorite;

    private Integer rangPriorite;

    private LocalDate dateDemande;
}