package com.creditscoring.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotRequest {

    private String message;
    private Long utilisateurId;
}
