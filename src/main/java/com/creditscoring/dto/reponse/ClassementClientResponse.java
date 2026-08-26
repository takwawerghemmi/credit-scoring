package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassementClientResponse {

    private Integer rang;

    private String client;

    private Long nombreCredits;

    private Double montantAccorde;

    private Double scoreCredit;

}