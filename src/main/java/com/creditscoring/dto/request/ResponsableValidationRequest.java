package com.creditscoring.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponsableValidationRequest {

    @NotNull(message = "La décision est obligatoire")
    private Boolean decision;

    @NotBlank(message = "Le commentaire est obligatoire")
    private String commentaire;
}