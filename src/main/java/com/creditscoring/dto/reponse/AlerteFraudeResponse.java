package com.creditscoring.dto.reponse;

import com.creditscoring.enums.TypeFraude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteFraudeResponse {

    private Long id;
    private TypeFraude type;
    private String description;
    private String severite;
    private LocalDateTime dateDetection;
    private Boolean traitee;
    private Long demandeId;
}
