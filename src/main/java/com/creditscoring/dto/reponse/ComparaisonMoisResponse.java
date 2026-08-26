package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparaisonMoisResponse {

    private Integer mois;

    private Long nombreCredits;

    private Double montantTotal;

    private Double montantMoyen;

    private Double evolution;

}