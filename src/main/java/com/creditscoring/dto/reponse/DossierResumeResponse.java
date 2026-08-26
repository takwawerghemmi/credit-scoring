package com.creditscoring.dto.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DossierResumeResponse {

    private Long demandeId;
    private String nomClient;
    private String resumeScore;
    private String resumeDocuments;
    private String resumeRisque;
    private String resumeHistorique;
    private String recommandationGenerale;
    private String typeCreditRecommande;
}
