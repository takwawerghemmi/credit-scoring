package com.creditscoring.repository;

import com.creditscoring.entity.ValidationDemande;
import com.creditscoring.enums.NiveauValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValidationDemandeRepository extends JpaRepository<ValidationDemande, Long> {

    List<ValidationDemande> findByDemandeCreditId(Long demandeId);

    Optional<ValidationDemande> findByDemandeCreditIdAndNiveau(Long demandeId, NiveauValidation niveau);
}
