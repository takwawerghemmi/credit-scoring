package com.creditscoring.repository;

import com.creditscoring.entity.Echeance;
import com.creditscoring.enums.StatutEcheance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EcheanceRepository extends JpaRepository<Echeance, Long> {

    List<Echeance> findByContratId(Long contratId);

    List<Echeance> findByContratIdAndStatut(Long contratId, StatutEcheance statut);

    List<Echeance> findByStatutAndDateEcheanceBefore(StatutEcheance statut, LocalDate date);

    long countByContratIdAndStatut(Long contratId, StatutEcheance statut);
}
