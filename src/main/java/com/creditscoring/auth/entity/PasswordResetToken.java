package com.creditscoring.auth.entity;

import com.creditscoring.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private LocalDateTime expirationDate;

    @Builder.Default
    private Boolean used = false;

    @PrePersist
    public void prePersist() {
        token = UUID.randomUUID().toString();
        expirationDate = LocalDateTime.now().plusMinutes(30);
    }

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }

}