package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplicationFacteurResponse {

    private Long demandeCreditId;

    private Double creditScore;

    private String niveauRisque;

    private Double probabiliteDefaut;

    private List<FacteurResponse> facteurs;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacteurResponse {

        private String feature;

        private String impact;

        private Double contribution;
    }
}