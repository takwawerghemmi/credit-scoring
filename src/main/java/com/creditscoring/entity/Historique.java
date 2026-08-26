package com.creditscoring.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historiques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Historique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private LocalDateTime dateAction;

    @Column(length = 1000)
    private String description;

    private String entite;

    private Long entiteId;

    @Column(length = 2000)
    private String ancienneValeur;

    @Column(length = 2000)
    private String nouvelleValeur;

    private String adresseIp;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (dateAction == null) {
            dateAction = LocalDateTime.now();
        }
    }
}