package com.creditscoring.dto.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RisqueAnalyseResponse {

    private Long clientId;
    private String nomComplet;
    private String niveauRisque;
    private Double scoreRisque;
    private Double ratioEndettement;
    private Double scoreConfiance;
    private String recommandation;
    private String details;
}
