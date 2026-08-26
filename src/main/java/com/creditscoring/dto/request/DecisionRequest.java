package com.creditscoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DecisionRequest {

    @NotNull
    private Long creditScoreId;

    @NotNull
    private Boolean accepte;

    @NotBlank
    private String commentaire;
}