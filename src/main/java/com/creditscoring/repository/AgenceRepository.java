package com.creditscoring.repository;

import com.creditscoring.entity.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agence, Long> {

    Optional<Agence> findByCodeAgence(String codeAgence);

    List<Agence> findByBanqueId(Long banqueId);
    @Query("""
SELECT
a.nom,
COUNT(d),
COALESCE(SUM(d.montantAccorde),0)
FROM Agence a
JOIN a.banque b
JOIN DemandeCredit d ON d.banque.id = b.id
GROUP BY a.id, a.nom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> topAgences();
    @Query("""
SELECT
a.nom,
COUNT(d)
FROM Agence a
JOIN a.banque b
JOIN DemandeCredit d ON d.banque.id = b.id
GROUP BY a.id, a.nom
ORDER BY COUNT(d) DESC
""")
    List<Object[]> repartitionParAgence();

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
    List<Object[]> comparaisonAgences();
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
    List<Object[]> classementAgences();





}
