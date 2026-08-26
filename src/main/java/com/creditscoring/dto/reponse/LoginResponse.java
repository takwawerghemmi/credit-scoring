package com.creditscoring.dto.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;

    private String type;

    private Long utilisateurId;

    private String nom;

    private String prenom;

    private String email;

    private String role;

}