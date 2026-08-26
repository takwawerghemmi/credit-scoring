package com.creditscoring.auth.entity;

import com.creditscoring.entity.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "email_verification_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false, length = 6)
    private String code;

    @OneToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Builder.Default
    private Boolean verified = false;

    @PrePersist
    public void prePersist() {

        if (token == null) {
            token = UUID.randomUUID().toString();
        }

        if (code == null) {
            code = String.format(
                    "%06d",
                    ThreadLocalRandom.current()
                            .nextInt(0, 1_000_000)
            );
        }

        if (expirationDate == null) {
            expirationDate =
                    LocalDateTime.now().plusHours(24);
        }

        if (verified == null) {
            verified = false;
        }
    }

    public boolean isExpired() {
        return expirationDate.isBefore(
                LocalDateTime.now()
        );
    }
}