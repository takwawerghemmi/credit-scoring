package com.creditscoring.entity;

import com.creditscoring.enums.TypeFraude;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertes_fraude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteFraude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeFraude type;

    @Column(length = 1000)
    private String description;

    private String severite;

    private LocalDateTime dateDetection;

    private Boolean traitee;

    @ManyToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;

    @PrePersist
    public void prePersist() {
        if (dateDetection == null) {
            dateDetection = LocalDateTime.now();
        }
        if (traitee == null) {
            traitee = false;
        }
    }
}
