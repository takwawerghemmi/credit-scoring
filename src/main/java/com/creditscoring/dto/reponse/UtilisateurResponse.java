package com.creditscoring.dto.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurResponse {

    private Long id;

    private String nom;

    private String prenom;

    private String email;

    private String telephone;

    private String adresse;

    private Boolean actif;

    private String role;
}