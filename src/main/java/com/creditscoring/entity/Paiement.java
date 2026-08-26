package com.creditscoring.entity;

import com.creditscoring.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "paiements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montant;

    private LocalDate datePaiement;

    @Enumerated(EnumType.STRING)
    private StatutPaiement statut;

    private String methodePaiement;

    private Double penalite;

    private Integer joursRetard;

    @ManyToOne
    @JoinColumn(name = "contrat_id")
    private Contrat contrat;

    @ManyToOne
    @JoinColumn(name = "echeance_id")
    private Echeance echeance;
}