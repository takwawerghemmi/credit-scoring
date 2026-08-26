package com.creditscoring.repository;

import com.creditscoring.entity.Conseiller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConseillerRepository extends JpaRepository<Conseiller, Long> {

    Optional<Conseiller> findByMatricule(String matricule);

    boolean existsByMatricule(String matricule);

}
