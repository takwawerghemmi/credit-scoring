package com.creditscoring.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administrateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Administrateur extends Utilisateur {

    @Column(nullable = false)
    private String matricule;

    private String departement;

}