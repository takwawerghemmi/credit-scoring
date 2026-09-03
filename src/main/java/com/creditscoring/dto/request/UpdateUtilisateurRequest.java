package com.creditscoring.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;

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

    // ==========================
    // Données spécifiques Client
    // ==========================

    private String cin;
    private LocalDate dateNaissance;
    private String profession;
    private String typeContratTravail;
    private String situationFamiliale;
    private Double dettesExistantes;
    private Double revenuMensuel;
    private Integer ancienneteEmploi;
    private Integer nombrePersonnesACharge;
}