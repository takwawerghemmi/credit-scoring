package com.creditscoring.dto.reponse;

import lombok.Data;

@Data
public class DecisionResponse {

    private Boolean accepte;

    private String commentaire;

}