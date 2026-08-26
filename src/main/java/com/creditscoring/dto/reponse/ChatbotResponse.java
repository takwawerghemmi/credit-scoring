package com.creditscoring.dto.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponse {

    private String reponse;
    private String categorie;
}
