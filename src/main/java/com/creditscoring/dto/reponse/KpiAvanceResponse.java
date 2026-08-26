package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class KpiAvanceResponse {

    private Long demandesAujourdHui;
    private Long demandesCetteSemaine;
    private Long demandesCeMois;

    private Double croissanceMensuelle;

    private Double montantMoyen;

    private Double tauxAcceptation;

    private Double tauxFraude;

}