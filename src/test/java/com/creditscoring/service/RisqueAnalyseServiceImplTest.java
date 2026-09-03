package com.creditscoring.service;

import com.creditscoring.entity.Client;
import com.creditscoring.entity.DemandeCredit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RisqueAnalyseServiceImplTest {

    private final RisqueAnalyseServiceImpl service =
            new RisqueAnalyseServiceImpl();

    @Test
    void shouldReturnLowRisk() {

        Client client = Client.builder()
                .revenuMensuel(10000.0)
                .dettesExistantes(0.0)
                .build();

        DemandeCredit demande = new DemandeCredit();

        demande.setClient(client);
        demande.setMontant(2000.0);

        String resultat =
                service.analyserRisque(demande);

        assertEquals(
                "RISQUE FAIBLE",
                resultat
        );
    }

    @Test
    void shouldReturnMediumRisk() {

        Client client = Client.builder()
                .revenuMensuel(10000.0)
                .dettesExistantes(2000.0)
                .build();

        DemandeCredit demande = new DemandeCredit();

        demande.setClient(client);
        demande.setMontant(3000.0);

        String resultat =
                service.analyserRisque(demande);

        assertEquals(
                "RISQUE MOYEN",
                resultat
        );
    }

    @Test
    void shouldReturnHighRisk() {

        Client client = Client.builder()
                .revenuMensuel(3500.0)
                .dettesExistantes(1000.0)
                .build();

        DemandeCredit demande = new DemandeCredit();

        demande.setClient(client);
        demande.setMontant(20000.0);

        String resultat =
                service.analyserRisque(demande);

        assertEquals(
                "RISQUE ELEVE",
                resultat
        );
    }

    @Test
    void shouldReturnHighRiskWhenIncomeIsZero() {

        Client client = Client.builder()
                .revenuMensuel(0.0)
                .dettesExistantes(0.0)
                .build();

        DemandeCredit demande = new DemandeCredit();

        demande.setClient(client);
        demande.setMontant(20000.0);

        String resultat =
                service.analyserRisque(demande);

        assertEquals(
                "RISQUE ELEVE",
                resultat
        );
    }
}
