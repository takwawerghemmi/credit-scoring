package com.creditscoring.entity;
import lombok.Builder;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "conseillers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Conseiller extends Utilisateur {

    @Column(nullable = false)
    private String matricule;

    private String specialite;

    @Builder.Default
    private Integer nombreDemandesEnCours = 0;

    @Builder.Default
    private Double limiteAutorisation = 30000.0;
    @ManyToOne
    @JoinColumn(name = "banque_id")
    private Banque banque;

    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

}