package com.creditscoring.repository;

import com.creditscoring.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {

    java.util.Optional<Client> findByEmail(String email);

    @Query("SELECT COUNT(c) FROM Client c")
    Long nombreClients();

    @Query("""
    SELECT c.profession, COUNT(c)
    FROM Client c
    GROUP BY c.profession
    """)
    List<Object[]> clientsParProfession();

    @Query("""
    SELECT c.situationFamiliale, COUNT(c)
    FROM Client c
    GROUP BY c.situationFamiliale
    """)
    List<Object[]> clientsParSituationFamiliale();
    @Query("SELECT AVG(c.scoreConfiance) FROM Client c")
    Double scoreCreditMoyen();

    @Query("SELECT AVG(c.revenuMensuel) FROM Client c")
    Double revenuMoyen();

    @Query("""
SELECT c.scoreConfiance,
COUNT(c)
FROM Client c
GROUP BY c.scoreConfiance
ORDER BY c.scoreConfiance
""")
    List<Object[]> repartitionScoreCredit();
    @Query("""
SELECT
CONCAT(c.nom,' ',c.prenom),
COUNT(d),
COALESCE(SUM(d.montantAccorde),0)
FROM Client c
JOIN DemandeCredit d ON d.client.id = c.id
GROUP BY c.id,c.nom,c.prenom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> topClients();

    @Query("""
SELECT
CONCAT(c.nom,' ',c.prenom),
COUNT(d),
COALESCE(SUM(d.montantAccorde),0),
COALESCE(AVG(c.scoreConfiance),0)
FROM DemandeCredit d
JOIN d.client c
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
GROUP BY c.id,c.nom,c.prenom,c.scoreConfiance
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> classementClients();






}