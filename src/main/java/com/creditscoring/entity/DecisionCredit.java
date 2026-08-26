package com.creditscoring.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "decision_credit")
public class DecisionCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean accepte;

    private String commentaire;

    @OneToOne
    @JoinColumn(name = "credit_score_id")
    private CreditScore creditScore;

    public DecisionCredit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getAccepte() {
        return accepte;
    }

    public void setAccepte(Boolean accepte) {
        this.accepte = accepte;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public CreditScore getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(CreditScore creditScore) {
        this.creditScore = creditScore;
    }
}