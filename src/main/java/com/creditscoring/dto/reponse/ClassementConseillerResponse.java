package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassementConseillerResponse {

    private Integer rang;

    private String conseiller;

    private Long demandes;

    private Long approuvees;

    private Long refusees;

    private Double tauxAcceptation;

    private Double montantAccorde;

}