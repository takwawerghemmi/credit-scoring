package com.creditscoring.entity;

import com.creditscoring.enums.NiveauValidation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "validations_demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;

    @ManyToOne
    @JoinColumn(name = "validateur_id")
    private Utilisateur validateur;

    @Enumerated(EnumType.STRING)
    private NiveauValidation niveau;

    private Boolean decision;

    @Column(length = 1000)
    private String commentaire;

    private LocalDateTime dateValidation;

    @PrePersist
    public void prePersist() {
        if (dateValidation == null) {
            dateValidation = LocalDateTime.now();
        }
    }
}
