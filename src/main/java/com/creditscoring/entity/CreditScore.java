package com.creditscoring.entity;

import com.creditscoring.enums.NiveauRisque;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "credit_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;

    @Enumerated(EnumType.STRING)
    private NiveauRisque niveauRisque;

    private LocalDateTime dateCalcul;

    private Double scoreConfiance;

    @Column(length = 2000)
    private String detailsScore;

    @Column(length = 1000)
    private String recommandation;

    @OneToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;
}
