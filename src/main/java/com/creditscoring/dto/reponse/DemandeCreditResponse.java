package com.creditscoring.dto.reponse;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeCreditResponse {

    private Long id;

    private Double montant;

    private Integer duree;

    private String typeCredit;

    private String statut;

    private Double revenuMensuel;

    private Double chargesMensuelles;

    private LocalDate dateDemande;

    private Long clientId;

    private Long conseillerId;

    private Long banqueId;

}