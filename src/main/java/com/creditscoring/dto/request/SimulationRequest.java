package com.creditscoring.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimulationRequest {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "1000", message = "Le montant minimum est de 1000 DT")
    private Double montant;

    @NotNull(message = "La durée est obligatoire")
    @Min(value = 1, message = "La durée minimale est de 1 mois")
    @Max(value = 360, message = "La durée maximale est de 360 mois")
    private Integer dureeMois;

    @NotNull(message = "Le taux annuel est obligatoire")
    @DecimalMin(value = "0.1", message = "Le taux doit être positif")
    private Double tauxAnnuel;

}