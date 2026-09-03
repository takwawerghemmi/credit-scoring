package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRecentDemandeDTO {

    private Long id;
    private Double montant;
    private Integer duree;
    private String typeCredit;
    private String statut;
    private Double revenuMensuel;
    private Double chargesMensuelles;
    private LocalDate dateDemande;
    private Double montantAccorde;
    private String motifRefus;
    private String commentaireAnalyse;
}