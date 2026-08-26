package com.creditscoring.entity;

import com.creditscoring.enums.StatutEcheance;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "echeances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Echeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer numero;

    private Double montant;

    private LocalDate dateEcheance;

    private LocalDate datePaiement;

    @Enumerated(EnumType.STRING)
    private StatutEcheance statut;

    private Double penalite;

    @ManyToOne
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    @PrePersist
    public void prePersist() {
        if (statut == null) {
            statut = StatutEcheance.EN_ATTENTE;
        }
        if (penalite == null) {
            penalite = 0.0;
        }
    }
}
