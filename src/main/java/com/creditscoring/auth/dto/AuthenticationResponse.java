package com.creditscoring.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationResponse {

    private String accessToken;

    private String refreshToken;

    private String type = "Bearer";

    private String email;

    private String role;

}