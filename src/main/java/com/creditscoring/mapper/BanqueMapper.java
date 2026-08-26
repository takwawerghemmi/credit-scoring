package com.creditscoring.mapper;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.reponse.BanqueResponse;
import com.creditscoring.entity.Banque;

public class BanqueMapper {

    public static Banque toEntity(BanqueRequest request){

        return Banque.builder()
                .nom(request.getNom())
                .codeBanque(request.getCodeBanque())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .siteWeb(request.getSiteWeb())
                .build();

    }

    public static BanqueResponse toResponse(Banque banque){

        return BanqueResponse.builder()
                .id(banque.getId())
                .nom(banque.getNom())
                .codeBanque(banque.getCodeBanque())
                .adresse(banque.getAdresse())
                .telephone(banque.getTelephone())
                .email(banque.getEmail())
                .siteWeb(banque.getSiteWeb())
                .build();

    }

}