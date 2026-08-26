package com.creditscoring.repository;

import com.creditscoring.entity.Contrat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContratRepository
        extends JpaRepository<Contrat, Long> {

    Optional<Contrat> findByDemandeCreditId(Long demandeId);
}