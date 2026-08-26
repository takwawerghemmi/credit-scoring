package com.creditscoring.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditScoreRequest {

    @NotNull
    private Long demandeCreditId;

}