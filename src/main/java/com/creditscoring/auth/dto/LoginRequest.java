package com.creditscoring.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String motDePasse;

    private String captchaId;

    private String captchaCode;
}