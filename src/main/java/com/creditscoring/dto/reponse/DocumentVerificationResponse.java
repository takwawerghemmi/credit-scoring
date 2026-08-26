package com.creditscoring.dto.reponse;

import com.creditscoring.enums.StatutDocument;
import com.creditscoring.enums.TypeDocument;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentVerificationResponse {

    private Long demandeId;
    private boolean complet;
    private List<DocumentStatus> documents;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentStatus {
        private TypeDocument typeDocument;
        private boolean present;
        private StatutDocument statut;
        private String message;
    }
}
