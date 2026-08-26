package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationResponse {

    // Montant demandé
    private Double montant;

    // Durée en mois
    private Integer dureeMois;

    // Taux annuel (%)
    private Double tauxAnnuel;

    // Mensualité calculée
    private Double mensualite;

    // Total des intérêts
    private Double interetsTotaux;

    // Coût total (capital + intérêts)
    private Double coutTotal;

    // Tableau d'amortissement
    private List<AmortissementResponse> echeances;

}