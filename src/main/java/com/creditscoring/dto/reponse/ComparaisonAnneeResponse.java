package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComparaisonAnneeResponse {

    private Integer annee;

    private Long demandes;

    private Long approuvees;

    private Long refusees;

    private Double montantAccorde;

    private Double tauxAcceptation;

}