package com.creditscoring.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationRequest {

    private Long demandeId;
    private Long validateurId;
    private Boolean decision;
    private String commentaire;
}
