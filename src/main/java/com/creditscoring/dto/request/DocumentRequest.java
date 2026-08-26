package com.creditscoring.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentRequest {

    private String nom;

    private String type;

    private String cheminFichier;

    private Long demandeCreditId;

    private String statut;
    private LocalDate dateDemande;
}