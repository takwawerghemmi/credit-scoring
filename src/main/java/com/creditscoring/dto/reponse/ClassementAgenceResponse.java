package com.creditscoring.dto.reponse;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassementAgenceResponse {

    private Integer rang;
    private String agence;
    private Long demandes;
    private Long approuvees;
    private Long refusees;
    private Double montant;

}