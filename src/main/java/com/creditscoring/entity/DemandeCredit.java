package com.creditscoring.entity;

import com.creditscoring.enums.StatutDemande;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "demandes_credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double montant;

    @Column(nullable = false)
    private Integer duree;

    @Column(nullable = false)
    private String typeCredit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statut;

    private Double revenuMensuel;

    private Double chargesMensuelles;

    private LocalDate dateDemande;

    private Double montantAccorde;

    private Double tauxInteret;

    @Column(length = 1000)
    private String motifRefus;

    @Column(length = 1000)
    private String commentaireAnalyse;

    private LocalDateTime dateAnalyse;

    private LocalDateTime dateDecision;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "conseiller_id")
    private Conseiller conseiller;

    @ManyToOne
    @JoinColumn(name = "banque_id")
    private Banque banque;

    @JsonIgnore
    @OneToMany(mappedBy = "demandeCredit", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Document> documents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        dateDemande = LocalDate.now();
        if (statut == null) {
            statut = StatutDemande.BROUILLON;
        }
    }
}