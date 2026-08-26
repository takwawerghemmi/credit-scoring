package com.creditscoring.dto.reponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgenceResponse {

    private Long id;

    private String nom;

    private String codeAgence;

    private String adresse;

    private String telephone;

    private Long banqueId;

    private String banqueNom;
}