package com.creditscoring.repository;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.entity.DemandeCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DemandeCreditRepository extends JpaRepository<DemandeCredit, Long> {

    // ==========================
    // Requêtes de base
    // ==========================
    List<DemandeCredit> findByClientId(Long clientId);
List<DemandeCredit> findByConseillerId(Long conseillerId);
List<DemandeCredit> findByResponsableId(Long responsableId);
// ==========================
// KPI CLIENT
// ==========================

@Query("""
    SELECT COUNT(d)
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
""")
Long nombreDemandesClient(@Param("clientId") Long clientId);

@Query("""
    SELECT COUNT(d)
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
    AND d.statut = com.creditscoring.enums.StatutDemande.EN_ANALYSE
""")
Long nombreDemandesEnAnalyseClient(@Param("clientId") Long clientId);

@Query("""
    SELECT COUNT(d)
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
    AND d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
""")
Long nombreDemandesApprouveesClient(@Param("clientId") Long clientId);

@Query("""
    SELECT COUNT(d)
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
    AND d.statut = com.creditscoring.enums.StatutDemande.REFUSEE
""")
Long nombreDemandesRefuseesClient(@Param("clientId") Long clientId);

@Query("""
    SELECT COALESCE(SUM(d.montant), 0)
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
""")
Double montantTotalDemandeClient(@Param("clientId") Long clientId);

@Query("""
    SELECT COALESCE(SUM(d.montantAccorde), 0)
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
    AND d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
""")
Double montantTotalAccordeClient(@Param("clientId") Long clientId);

@Query("""
    SELECT d
    FROM DemandeCredit d
    WHERE d.client.id = :clientId
    ORDER BY d.dateDemande DESC
""")
List<DemandeCredit> dernieresDemandesClient(@Param("clientId") Long clientId);


    List<DemandeCredit> findByStatut(StatutDemande statut);

    // ==========================
    // KPI Dashboard
    // ==========================

    @Query("SELECT COUNT(d) FROM DemandeCredit d")
    Long nombreDemandes();

    @Query("""
            SELECT COUNT(d)
            FROM DemandeCredit d
            WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
            """)
    Long nombreCreditsApprouves();

    @Query("""
            SELECT COUNT(d)
            FROM DemandeCredit d
            WHERE d.statut = com.creditscoring.enums.StatutDemande.REFUSEE
            """)
    Long nombreCreditsRefuses();

    @Query("""
            SELECT COUNT(d)
            FROM DemandeCredit d
            WHERE d.statut = com.creditscoring.enums.StatutDemande.EN_ANALYSE
            """)
    Long nombreCreditsEnAnalyse();

    @Query("""
            SELECT COALESCE(SUM(d.montantAccorde), 0)
            FROM DemandeCredit d
            WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
            """)
    Double montantTotalAccorde();

    // ==========================
    // Statistiques BI
    // ==========================

    @Query("""
            SELECT MONTH(d.dateDemande), COUNT(d)
            FROM DemandeCredit d
            GROUP BY MONTH(d.dateDemande)
            ORDER BY MONTH(d.dateDemande)
            """)
    List<Object[]> demandesParMois();

    @Query("""
            SELECT d.typeCredit, COUNT(d)
            FROM DemandeCredit d
            GROUP BY d.typeCredit
            """)
    List<Object[]> demandesParTypeCredit();

    @Query("""
            SELECT d.statut, COUNT(d)
            FROM DemandeCredit d
            GROUP BY d.statut
            """)
    List<Object[]> demandesParStatut();
    @Query("SELECT MAX(d.montantAccorde) FROM DemandeCredit d")
    Double montantMaximumDemande();

    @Query("SELECT MIN(d.montantAccorde) FROM DemandeCredit d")
    Double montantMinimumDemande();
    @Query("""
SELECT MONTH(d.dateDecision),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
GROUP BY MONTH(d.dateDecision)
ORDER BY MONTH(d.dateDecision)
""")
    List<Object[]> evolutionMontantsAccordes();
    @Query("""
SELECT
CONCAT(c.nom,' ',c.prenom),
COUNT(d),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
JOIN d.conseiller c
GROUP BY c.id, c.nom, c.prenom
ORDER BY COUNT(d) DESC
""")
    List<Object[]> performanceConseillers();
    @Query("""
SELECT
CONCAT(c.nom,' ',c.prenom),
COUNT(d),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
JOIN d.conseiller c
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
GROUP BY c.id,c.nom,c.prenom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> topConseillers();
    @Query("""
SELECT
d.id,
CONCAT(c.nom,' ',c.prenom),
d.typeCredit,
d.montant,
d.statut,
d.dateDemande
FROM DemandeCredit d
JOIN d.client c
ORDER BY d.dateDemande DESC
""")
    List<Object[]> dernieresDemandes();
    @Query("""
SELECT d
FROM DemandeCredit d
WHERE
(:statut IS NULL OR d.statut = :statut)
AND (:typeCredit IS NULL OR d.typeCredit = :typeCredit)
ORDER BY d.dateDemande DESC
""")
    List<DemandeCredit> filtrerDemandes(
            @Param("statut") StatutDemande statut,
            @Param("typeCredit") String typeCredit
    );
    @Query("""
SELECT AVG(d.montantAccorde)
FROM DemandeCredit d
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
""")
    Double montantMoyenAccordeDashboard();

    @Query("""
SELECT MAX(d.montantAccorde)
FROM DemandeCredit d
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
""")
    Double montantMaximumAccorde();
    @Query("""
SELECT MIN(d.montantAccorde)
FROM DemandeCredit d
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
""")
    Double montantMinimumAccorde();
    @Query("""
SELECT COUNT(d)
FROM DemandeCredit d
WHERE d.dateDemande = CURRENT_DATE
""")
    Long demandesAujourdHui();
    @Query("""
SELECT
MONTH(d.dateDecision),
COUNT(d),
COALESCE(SUM(d.montantAccorde),0),
COALESCE(AVG(d.montantAccorde),0)
FROM DemandeCredit d
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
GROUP BY MONTH(d.dateDecision)
ORDER BY MONTH(d.dateDecision)
""")
    List<Object[]> statistiquesCreditsParMois();


    @Query("""
SELECT
MONTH(d.dateDemande),
COUNT(d),
COALESCE(SUM(d.montantAccorde),0),
COALESCE(AVG(d.montantAccorde),0)
FROM DemandeCredit d
WHERE d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE
GROUP BY MONTH(d.dateDemande)
ORDER BY MONTH(d.dateDemande)
""")
    List<Object[]> comparaisonParMois();
    @Query("""
SELECT
YEAR(d.dateDemande),
COUNT(d),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE THEN 1 ELSE 0 END),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.REFUSEE THEN 1 ELSE 0 END),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
GROUP BY YEAR(d.dateDemande)
ORDER BY YEAR(d.dateDemande)
""")
    List<Object[]> comparaisonParAnnee();
    @Query("""
SELECT COUNT(d)
FROM DemandeCredit d
WHERE YEARWEEK(d.dateDemande)=YEARWEEK(CURRENT_DATE)
""")
    Long demandesCetteSemaine();@Query("""
SELECT COUNT(d)
FROM DemandeCredit d
WHERE MONTH(d.dateDemande)=MONTH(CURRENT_DATE)
AND YEAR(d.dateDemande)=YEAR(CURRENT_DATE)
""")
    Long demandesCeMois();


    @Query("""
SELECT COUNT(d)
FROM DemandeCredit d
WHERE d.statut=com.creditscoring.enums.StatutDemande.APPROUVEE
""")
    Long creditsAcceptes();




    @Query("""
SELECT
MONTH(d.dateDemande),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
WHERE YEAR(d.dateDemande)=YEAR(CURRENT_DATE)
GROUP BY MONTH(d.dateDemande)
ORDER BY MONTH(d.dateDemande)
""")
    List<Object[]> evolutionMensuelleMontants();
    @Query("""
SELECT
CONCAT(c.nom,' ',c.prenom),
COUNT(d),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.APPROUVEE THEN 1 ELSE 0 END),
SUM(CASE WHEN d.statut = com.creditscoring.enums.StatutDemande.REFUSEE THEN 1 ELSE 0 END),
COALESCE(SUM(d.montantAccorde),0)
FROM DemandeCredit d
JOIN d.conseiller c
GROUP BY c.id,c.nom,c.prenom
ORDER BY SUM(d.montantAccorde) DESC
""")
    List<Object[]> classementConseillers();
















}
