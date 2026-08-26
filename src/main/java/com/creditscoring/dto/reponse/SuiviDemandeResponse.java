package com.creditscoring.dto.reponse;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuiviDemandeResponse {

    private Long demandeId;
    private String statutActuel;
    private List<EtapeSuivi> etapes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EtapeSuivi {
        private String etape;
        private String statut;
        private LocalDateTime date;
        private boolean complete;
    }
}
