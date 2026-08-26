package com.creditscoring.dto.request;

import java.time.LocalDate;

public class ContratRequest {

    private String numeroContrat;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Double montant;
    private Long utilisateurId;
    private Long demandeCreditId;

    public ContratRequest() {
    }

    public String getNumeroContrat() {
        return numeroContrat;
    }

    public void setNumeroContrat(String numeroContrat) {
        this.numeroContrat = numeroContrat;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public Long getDemandeCreditId() {
        return demandeCreditId;
    }

    public void setDemandeCreditId(Long demandeCreditId) {
        this.demandeCreditId = demandeCreditId;
    }
}