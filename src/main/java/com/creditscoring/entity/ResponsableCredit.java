package com.creditscoring.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "responsables_credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ResponsableCredit extends Utilisateur {

    @Column(nullable = false)
    private String matricule;

    private Double limiteAutorisation;

    @ManyToOne
    @JoinColumn(name = "agence_id")
    private Agence agence;

    @PrePersist
    public void prePersistResponsable() {
        if (limiteAutorisation == null) {
            limiteAutorisation = 150000.0;
        }
    }
}
