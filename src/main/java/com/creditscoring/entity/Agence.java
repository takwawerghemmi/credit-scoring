package com.creditscoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private String adresse;

    @Column(nullable = false, unique = true)
    private String codeAgence;

    private String telephone;

    @ManyToOne
    @JoinColumn(name = "banque_id")
    private Banque banque;

    @JsonIgnore
    @OneToMany(
            mappedBy = "agence",
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<Conseiller> conseillers =
            new ArrayList<>();
}