package com.creditscoring.mapper;

import com.creditscoring.dto.reponse.GarantieResponse;
import com.creditscoring.entity.Garantie;

public class GarantieMapper {

    private GarantieMapper() {
    }

    public static GarantieResponse toResponse(Garantie garantie) {

        return GarantieResponse.builder()
                .id(garantie.getId())
                .type(garantie.getType())
                .valeur(garantie.getValeur())
                .description(garantie.getDescription())
                .demandeCreditId(
                        garantie.getDemandeCredit() != null
                                ? garantie.getDemandeCredit().getId()
                                : null
                )
                .build();
    }
}