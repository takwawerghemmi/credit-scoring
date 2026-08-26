package com.creditscoring.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUtilisateurRequest {

    private String nom;

    private String prenom;

    @Email
    private String email;

    private String telephone;

    private String adresse;

    private Boolean actif;
}