package com.creditscoring.dto.reponse;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassementBanqueResponse {

    private Integer rang;
    private String banque;
    private Long demandes;
    private Long approuvees;
    private Long refusees;
    private Double montant;

}