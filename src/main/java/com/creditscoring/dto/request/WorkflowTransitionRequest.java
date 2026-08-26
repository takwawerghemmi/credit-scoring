package com.creditscoring.dto.request;

import com.creditscoring.enums.StatutDemande;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransitionRequest {

    private StatutDemande nouveauStatut;
    private String commentaire;
}
