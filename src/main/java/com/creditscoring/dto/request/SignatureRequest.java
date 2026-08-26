package com.creditscoring.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignatureRequest {

    private Long contratId;
    private String signatureBase64;
}
