package com.creditscoring.dto.reponse;
import com.creditscoring.enums.NiveauRisque;
import lombok.Data;

@Data
public class CreditScoreResponse {

    private Long id;

    private Double score;

    private NiveauRisque niveauRisque;

    private String decision;

}
