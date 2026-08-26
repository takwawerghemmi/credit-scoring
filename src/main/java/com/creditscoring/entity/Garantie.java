package com.creditscoring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "garanties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garantie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private Double valeur;

    private String description;

    @ManyToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;
}