package com.creditscoring.entity;

import com.creditscoring.enums.CanalNotification;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Column(length = 1000)
    private String message;

    private LocalDateTime dateEnvoi;

    private boolean lu;

    @Enumerated(EnumType.STRING)
    private CanalNotification canal;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;

    @PrePersist
    public void prePersist() {
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
        }
        if (canal == null) {
            canal = CanalNotification.IN_APP;
        }
    }
}