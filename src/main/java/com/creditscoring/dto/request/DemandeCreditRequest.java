package com.creditscoring.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeCreditRequest {

    @NotNull
    @Min(1000)
    private Double montant;

    @NotNull
    @Min(1)
    private Integer duree;

    @NotNull
    private String typeCredit;

    @NotNull
    private Double revenuMensuel;

    @NotNull
    private Double chargesMensuelles;

    private Long clientId;

    @NotNull
    private Long banqueId;
}