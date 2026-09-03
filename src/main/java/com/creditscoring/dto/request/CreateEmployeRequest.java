package com.creditscoring.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateEmployeRequest {

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String motDePasse;

    private String telephone;

    private String adresse;

    @NotBlank
    private String matricule;

    private Long banqueId;

    private Long agenceId;
}