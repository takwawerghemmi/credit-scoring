package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmortissementResponse{

    // Numéro de l'échéance
    private Integer numero;

    // Mensualité
    private Double mensualite;

    // Part des intérêts
    private Double interets;

    // Part du capital remboursé
    private Double capital;

    // Capital restant
    private Double capitalRestant;

}