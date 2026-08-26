package com.creditscoring.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanqueRequest {

    @NotBlank
    private String nom;

    @NotBlank
    private String codeBanque;

    private String adresse;

    private String telephone;

    @Email
    private String email;

    private String siteWeb;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterClientRequest {

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

        @NotBlank
        private String cin;

        private LocalDate dateNaissance;

        private String profession;

        @NotNull
        private Double revenuMensuel;
    }
}
