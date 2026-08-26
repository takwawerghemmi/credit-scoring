package com.creditscoring.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "banques")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Banque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String codeBanque;

    private String adresse;

    private String telephone;

    private String email;

    private String siteWeb;
    @JsonIgnore
    @OneToMany(mappedBy = "banque", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Client> clients = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "banque", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Conseiller> conseillers = new ArrayList<>();

}