package com.creditscoring.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterClientRequest {

    @NotBlank
    private String nom;

    @NotBlank
    private String prenom;

    @Email
    private String email;

    @NotBlank
    private String motDePasse;

    private String telephone;

    private String adresse;

    private String cin;

    private String profession;

    private Double revenuMensuel;

}