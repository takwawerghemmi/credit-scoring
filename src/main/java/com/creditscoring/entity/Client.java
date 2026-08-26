package com.creditscoring.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name="clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Client extends Utilisateur{

    @Column(nullable=false)
    private String cin;

    private LocalDate dateNaissance;

    private String profession;



    private String typeContratTravail;

    private String situationFamiliale;
    @Builder.Default
    private Double dettesExistantes = 0.0;

    @Builder.Default
    private Double revenuMensuel = 0.0;

    @Builder.Default
    private Integer ancienneteEmploi = 0;

    @Builder.Default
    private Integer nombrePersonnesACharge = 0;
    private Double scoreConfiance;

    @ManyToOne
    @JoinColumn(name="banque_id")
    private Banque banque;

}