package com.creditscoring.repository;

import com.creditscoring.entity.Historique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueRepository extends JpaRepository<Historique, Long> {

    List<Historique> findByUtilisateurId(Long utilisateurId);

    List<Historique> findByAction(String action);
}