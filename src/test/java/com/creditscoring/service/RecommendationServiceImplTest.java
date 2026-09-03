package com.creditscoring.service;

import com.creditscoring.dto.reponse.RecommendationResponse;
import com.creditscoring.entity.DemandeCredit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationServiceImplTest {

    private final RecommendationServiceImpl service =
            new RecommendationServiceImpl();

    @Test
    void shouldRecommendAgreementForFavorableProfile() {

        DemandeCredit demande = new DemandeCredit();

        demande.setRevenuMensuel(3500.0);
        demande.setChargesMensuelles(500.0);
        demande.setMontant(20000.0);
        demande.setDuree(36);

        RecommendationResponse response =
                service.genererRecommandations(demande);

        assertEquals(
                "ACCORD POSSIBLE",
                response.getDecision()
        );

        assertTrue(
                response.getRecommandations()
                        .contains("Profil favorable.")
        );
    }

    @Test
    void shouldRequestAdditionalAnalysisForLowIncome() {

        DemandeCredit demande = new DemandeCredit();

        demande.setRevenuMensuel(2000.0);
        demande.setChargesMensuelles(300.0);
        demande.setMontant(20000.0);
        demande.setDuree(36);

        RecommendationResponse response =
                service.genererRecommandations(demande);

        assertEquals(
                "ANALYSE COMPLÉMENTAIRE",
                response.getDecision()
        );

        assertTrue(
                response.getRecommandations()
                        .contains(
                                "Revenu mensuel faible : vérifier la capacité de remboursement."
                        )
        );
    }

    @Test
    void shouldRequestAdditionalAnalysisForHighCharges() {

        DemandeCredit demande = new DemandeCredit();

        demande.setRevenuMensuel(3500.0);
        demande.setChargesMensuelles(2500.0);
        demande.setMontant(20000.0);
        demande.setDuree(36);

        RecommendationResponse response =
                service.genererRecommandations(demande);

        assertEquals(
                "ANALYSE COMPLÉMENTAIRE",
                response.getDecision()
        );

        assertTrue(
                response.getRecommandations()
                        .contains(
                                "Charges mensuelles élevées."
                        )
        );
    }

    @Test
    void shouldRecommendAdditionalGuaranteesForHighAmount() {

        DemandeCredit demande = new DemandeCredit();

        demande.setRevenuMensuel(5000.0);
        demande.setChargesMensuelles(500.0);
        demande.setMontant(200000.0);
        demande.setDuree(36);

        RecommendationResponse response =
                service.genererRecommandations(demande);

        assertTrue(
                response.getRecommandations()
                        .contains(
                                "Montant élevé : demander des garanties supplémentaires."
                        )
        );
    }

    @Test
    void shouldRecommendReviewForVeryLongDuration() {

        DemandeCredit demande = new DemandeCredit();

        demande.setRevenuMensuel(5000.0);
        demande.setChargesMensuelles(500.0);
        demande.setMontant(20000.0);
        demande.setDuree(121);

        RecommendationResponse response =
                service.genererRecommandations(demande);

        assertTrue(
                response.getRecommandations()
                        .contains(
                                "Durée très longue : revoir les conditions du prêt."
                        )
        );
    }
}
