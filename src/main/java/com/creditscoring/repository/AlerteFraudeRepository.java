package com.creditscoring.repository;

import com.creditscoring.entity.AlerteFraude;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlerteFraudeRepository extends JpaRepository<AlerteFraude, Long> {

    @Query("SELECT COUNT(a) FROM AlerteFraude a")
    Long nombreAlertesFraude();

    @Query("""
            SELECT a.type, COUNT(a)
            FROM AlerteFraude a
            GROUP BY a.type
            """)
    List<Object[]> statistiquesFraude();
    @Query("""
SELECT
a.id,
CONCAT(c.nom,' ',c.prenom),
a.type,
a.severite,
a.dateDetection
FROM AlerteFraude a
JOIN a.demandeCredit d
JOIN d.client c
ORDER BY a.dateDetection DESC
""")
    List<Object[]> dernieresAlertesFraude();

    @Query("""
SELECT COUNT(a)
FROM AlerteFraude a
""")
    Long nombreFraudes();










}