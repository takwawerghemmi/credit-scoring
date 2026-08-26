package com.creditscoring.repository;

import com.creditscoring.entity.Banque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BanqueRepository extends JpaRepository<Banque,Long>{

    Optional<Banque> findByCodeBanque(String codeBanque);

    boolean existsByCodeBanque(String codeBanque);
    @Query("""
SELECT
b.nom,
COUNT(d),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE THEN 1 ELSE 0 END),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.REFUSEE THEN 1 ELSE 0 END),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
JOIN d.banque b
GROUP BY b.id,b.nom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> comparaisonBanques();
    @Query("""
SELECT
b.nom,
COUNT(d),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE THEN 1 ELSE 0 END),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.REFUSEE THEN 1 ELSE 0 END),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
JOIN d.banque b
GROUP BY b.id,b.nom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> classementBanques();
    @Query("""
SELECT
a.nom,
COUNT(d),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE THEN 1 ELSE 0 END),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.REFUSEE THEN 1 ELSE 0 END),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
JOIN d.conseiller c
JOIN c.agence a
GROUP BY a.id,a.nom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> classementAgences();}
