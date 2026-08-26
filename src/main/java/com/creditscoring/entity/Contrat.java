package com.creditscoring.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.creditscoring.enums.StatutContrat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contrats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroContrat;

    private LocalDate dateDebut;

    private LocalDate dateFin;

    private Double montant;

    private Double tauxInteret;

    private Double mensualite;

    private Double coutTotal;

    @Enumerated(EnumType.STRING)
    private StatutContrat statut;

    @Column(length = 5000)
    private String signatureClient;

    private LocalDateTime dateSignature;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @OneToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;

    @JsonIgnore
    @OneToMany(mappedBy = "contrat", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Echeance> echeances = new ArrayList<>();
    @PrePersist
    public void prePersist() {
        if (statut == null) {
            statut = StatutContrat.ACTIF;
        }
    }
}