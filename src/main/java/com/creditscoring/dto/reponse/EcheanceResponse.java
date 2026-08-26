package com.creditscoring.dto.reponse;

import com.creditscoring.enums.StatutEcheance;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcheanceResponse {

    private Long id;
    private Integer numero;
    private Double montant;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private StatutEcheance statut;
    private Double penalite;
    private Long contratId;
}
