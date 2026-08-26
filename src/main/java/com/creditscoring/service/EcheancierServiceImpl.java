package com.creditscoring.service;

import com.creditscoring.dto.reponse.AmortissementResponse;
import com.creditscoring.dto.reponse.EcheanceResponse;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Echeance;
import com.creditscoring.enums.StatutEcheance;
import com.creditscoring.repository.ContratRepository;
import com.creditscoring.repository.EcheanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcheancierServiceImpl
        implements EcheancierService {

    private final EcheanceRepository echeanceRepository;
    private final ContratRepository contratRepository;

    // =====================================================
    // CALCUL THÉORIQUE DE L'AMORTISSEMENT
    // =====================================================

    @Override
    public List<AmortissementResponse> genererEcheancier(
            DemandeCredit demande
    ) {

        List<AmortissementResponse> echeancier =
                new ArrayList<>();

        if (demande == null) {
            throw new RuntimeException(
                    "Demande introuvable."
            );
        }

        if (demande.getMontant() == null) {
            throw new RuntimeException(
                    "Le montant de la demande est obligatoire."
            );
        }

        if (demande.getDuree() == null
                || demande.getDuree() <= 0) {

            throw new RuntimeException(
                    "La durée de la demande est invalide."
            );
        }

        double capital =
                demande.getMontant();

        int duree =
                demande.getDuree();

        double tauxAnnuel =
                demande.getTauxInteret() != null
                        ? demande.getTauxInteret()
                        : 0.0;

        double tauxMensuel =
                tauxAnnuel / 12.0 / 100.0;

        double mensualite;

        if (tauxMensuel == 0) {

            mensualite =
                    capital / duree;

        } else {

            mensualite =
                    (capital * tauxMensuel)
                            / (
                            1
                                    - Math.pow(
                                    1 + tauxMensuel,
                                    -duree
                            )
                    );
        }

        double restant =
                capital;

        for (int mois = 1;
             mois <= duree;
             mois++) {

            double interet =
                    restant * tauxMensuel;

            double amortissement =
                    mensualite - interet;

            restant -= amortissement;

            if (mois == duree) {
                restant = 0;
            }

            AmortissementResponse ligne =
                    new AmortissementResponse();

            ligne.setNumero(mois);

            ligne.setMensualite(
                    arrondir(mensualite)
            );

            ligne.setInterets(
                    arrondir(interet)
            );

            ligne.setCapital(
                    arrondir(amortissement)
            );

            ligne.setCapitalRestant(
                    arrondir(
                            Math.max(restant, 0)
                    )
            );

            echeancier.add(
                    ligne
            );
        }

        return echeancier;
    }

    // =====================================================
    // CRÉER LES ÉCHÉANCES RÉELLES EN BASE
    // =====================================================

    @Override
    @Transactional
    public List<EcheanceResponse> creerEcheances(
            Contrat contrat
    ) {

        if (contrat == null) {

            throw new RuntimeException(
                    "Contrat introuvable."
            );
        }

        if (contrat.getId() == null) {

            throw new RuntimeException(
                    "Le contrat doit être sauvegardé avant de créer les échéances."
            );
        }

        if (contrat.getDateDebut() == null) {

            throw new RuntimeException(
                    "La date de début du contrat est obligatoire."
            );
        }

        if (contrat.getMensualite() == null) {

            throw new RuntimeException(
                    "La mensualité du contrat est obligatoire."
            );
        }

        if (contrat.getDemandeCredit() == null) {

            throw new RuntimeException(
                    "La demande de crédit du contrat est introuvable."
            );
        }

        Integer duree =
                contrat.getDemandeCredit()
                        .getDuree();

        if (duree == null || duree <= 0) {

            throw new RuntimeException(
                    "La durée du contrat est invalide."
            );
        }

        // -----------------------------------------------------
        // Ne pas créer deux fois les mêmes échéances
        // -----------------------------------------------------

        List<Echeance> existantes =
                echeanceRepository.findByContratId(
                        contrat.getId()
                );

        if (!existantes.isEmpty()) {

            return convertirListe(
                    existantes
            );
        }

        List<Echeance> echeances =
                new ArrayList<>();

        for (int i = 1;
             i <= duree;
             i++) {

            Echeance echeance =
                    Echeance.builder()
                            .numero(i)
                            .montant(
                                    arrondir(
                                            contrat.getMensualite()
                                    )
                            )
                            .dateEcheance(
                                    contrat.getDateDebut()
                                            .plusMonths(i)
                            )
                            .datePaiement(null)
                            .statut(
                                    StatutEcheance.EN_ATTENTE
                            )
                            .penalite(0.0)
                            .contrat(contrat)
                            .build();

            echeances.add(
                    echeance
            );
        }

        List<Echeance> sauvegardees =
                echeanceRepository.saveAll(
                        echeances
                );

        return convertirListe(
                sauvegardees
        );
    }

    // =====================================================
    // RÉCUPÉRER LES ÉCHÉANCES D'UN CONTRAT
    // =====================================================

    @Override
    public List<EcheanceResponse> getEcheancesByContrat(
            Long contratId
    ) {

        if (!contratRepository.existsById(contratId)) {

            throw new RuntimeException(
                    "Contrat introuvable."
            );
        }

        List<Echeance> echeances =
                echeanceRepository.findByContratId(
                        contratId
                );

        return convertirListe(
                echeances
        );
    }

    // =====================================================
    // CONVERSION ENTITY -> RESPONSE
    // =====================================================

    private List<EcheanceResponse> convertirListe(
            List<Echeance> echeances
    ) {

        List<EcheanceResponse> responses =
                new ArrayList<>();

        for (Echeance echeance : echeances) {

            EcheanceResponse response =
                    EcheanceResponse.builder()
                            .id(echeance.getId())
                            .numero(echeance.getNumero())
                            .montant(echeance.getMontant())
                            .dateEcheance(
                                    echeance.getDateEcheance()
                            )
                            .datePaiement(
                                    echeance.getDatePaiement()
                            )
                            .statut(
                                    echeance.getStatut()
                            )
                            .penalite(
                                    echeance.getPenalite()
                            )
                            .contratId(
                                    echeance.getContrat() != null
                                            ? echeance.getContrat().getId()
                                            : null
                            )
                            .build();

            responses.add(
                    response
            );
        }

        return responses;
    }

    // =====================================================
    // ARRONDIR
    // =====================================================

    private double arrondir(
            double valeur
    ) {

        return Math.round(
                valeur * 100.0
        ) / 100.0;
    }
}