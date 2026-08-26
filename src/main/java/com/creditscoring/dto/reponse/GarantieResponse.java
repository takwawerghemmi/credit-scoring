package com.creditscoring.dto.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantieResponse {

    private Long id;

    private String type;

    private Double valeur;

    private String description;

    private Long demandeCreditId;

}