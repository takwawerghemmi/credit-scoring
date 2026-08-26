package com.creditscoring.entity;

import com.creditscoring.enums.StatutDocument;
import com.creditscoring.enums.TypeDocument;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String type;

    private String cheminFichier;

    @Enumerated(EnumType.STRING)
    private TypeDocument typeDocument;

    @Enumerated(EnumType.STRING)
    private StatutDocument statutDocument;

    private LocalDateTime dateUpload;

    private LocalDate dateExpiration;

    private Long tailleFichier;

    private String contentType;

    @ManyToOne
    @JoinColumn(name = "demande_credit_id")
    private DemandeCredit demandeCredit;

    @PrePersist
    public void prePersist() {

        if (dateUpload == null) {
            dateUpload = LocalDateTime.now();
        }

        if (statutDocument == null) {
            statutDocument = StatutDocument.EN_ATTENTE;
        }
    }
}