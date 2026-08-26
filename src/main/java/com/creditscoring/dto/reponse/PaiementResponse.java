package com.creditscoring.dto.reponse;

import com.creditscoring.enums.StatutPaiement;

import java.time.LocalDate;

public class PaiementResponse {

    private Long id;

    private Double montant;

    private LocalDate datePaiement;

    private StatutPaiement statut;

    private String methodePaiement;

    private Long contratId;

    private Long echeanceId;

    private Integer numeroEcheance;

    private LocalDate dateEcheance;

    public PaiementResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public Long getContratId() {
        return contratId;
    }

    public void setContratId(Long contratId) {
        this.contratId = contratId;
    }

    public Long getEcheanceId() {
        return echeanceId;
    }

    public void setEcheanceId(Long echeanceId) {
        this.echeanceId = echeanceId;
    }

    public Integer getNumeroEcheance() {
        return numeroEcheance;
    }

    public void setNumeroEcheance(Integer numeroEcheance) {
        this.numeroEcheance = numeroEcheance;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
}