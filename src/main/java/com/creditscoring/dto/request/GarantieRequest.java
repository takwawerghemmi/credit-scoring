package com.creditscoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GarantieRequest {

    @NotBlank
    private String type;

    @NotNull
    private Double valeur;

    private String description;

    @NotNull
    private Long demandeCreditId;

}