package com.creditscoring.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "directeurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Directeur extends Utilisateur {

    @Column(nullable = false)
    private String matricule;

    @ManyToOne
    @JoinColumn(name = "banque_id")
    private Banque banque;
}
