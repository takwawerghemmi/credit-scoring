package com.creditscoring.service;

import com.creditscoring.dto.reponse.AmortissementResponse;
import com.creditscoring.dto.request.SimulationRequest;
import com.creditscoring.dto.reponse.SimulationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationServiceImpl implements SimulationService {

    @Override
    public SimulationResponse simulerCredit(SimulationRequest request) {

        double montant = request.getMontant();
        int duree = request.getDureeMois();
        double tauxAnnuel = request.getTauxAnnuel();

        // Taux mensuel
        double tauxMensuel = (tauxAnnuel / 100) / 12;

        // Formule de la mensualité
        double mensualite;

        if (tauxMensuel == 0) {
            mensualite = montant / duree;
        } else {
            mensualite = montant *
                    (tauxMensuel * Math.pow(1 + tauxMensuel, duree))
                    / (Math.pow(1 + tauxMensuel, duree) - 1);
        }

        double capitalRestant = montant;
        double totalInterets = 0;

        List<AmortissementResponse> tableau = new ArrayList<>();

        for (int i = 1; i <= duree; i++) {

            double interets = capitalRestant * tauxMensuel;

            double capital = mensualite - interets;

            capitalRestant -= capital;

            if (capitalRestant < 0) {
                capitalRestant = 0;
            }

            totalInterets += interets;

            tableau.add(
                    new AmortissementResponse(
                            i,
                            arrondir(mensualite),
                            arrondir(interets),
                            arrondir(capital),
                            arrondir(capitalRestant)
                    )
            );

        }

        SimulationResponse response = new SimulationResponse();

        response.setMontant(arrondir(montant));
        response.setDureeMois(duree);
        response.setTauxAnnuel(tauxAnnuel);
        response.setMensualite(arrondir(mensualite));
        response.setInteretsTotaux(arrondir(totalInterets));
        response.setCoutTotal(arrondir(montant + totalInterets));
        response.setEcheances(tableau);

        return response;
    }

    private double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}