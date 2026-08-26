package com.creditscoring.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecommendationResponse {

    private String decision;

    private List<String> recommandations;

}