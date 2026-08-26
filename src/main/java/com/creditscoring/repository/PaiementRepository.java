package com.creditscoring.repository;

import com.creditscoring.entity.Paiement;
import com.creditscoring.enums.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository
        extends JpaRepository<Paiement, Long> {

    List<Paiement> findByStatut(
            StatutPaiement statut
    );

    List<Paiement> findByContratId(
            Long contratId
    );

    Optional<Paiement> findByEcheanceId(
            Long echeanceId
    );
}