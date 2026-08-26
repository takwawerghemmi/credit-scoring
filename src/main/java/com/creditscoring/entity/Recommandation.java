package com.creditscoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "recommandations")
public class Recommandation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String banque;

    private Double tauxInteret;

    private Integer duree;

    private Double montantMax;

    @OneToOne
    @JoinColumn(name = "credit_score_id")
    private CreditScore creditScore;

    public Recommandation() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBanque() {
        return banque;
    }

    public void setBanque(String banque) {
        this.banque = banque;
    }

    public Double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(Double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public Double getMontantMax() {
        return montantMax;
    }

    public void setMontantMax(Double montantMax) {
        this.montantMax = montantMax;
    }

    public CreditScore getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(CreditScore creditScore) {
        this.creditScore = creditScore;
    }
}