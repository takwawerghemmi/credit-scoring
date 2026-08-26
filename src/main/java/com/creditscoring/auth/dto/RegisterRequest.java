package com.creditscoring.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String nom;

    private String prenom;

    private String email;

    private String motDePasse;

    private String telephone;

    private String adresse;

    private String cin;

    private Double revenuMensuel;
}
