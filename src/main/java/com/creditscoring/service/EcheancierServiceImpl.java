package com.creditscoring.service;

import com.creditscoring.dto.reponse.AmortissementResponse;
import com.creditscoring.dto.reponse.EcheanceResponse;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Echeance;
import com.creditscoring.entity.ResponsableCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.StatutEcheance;
import com.creditscoring.repository.ContratRepository;
import com.creditscoring.repository.EcheanceRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EcheancierServiceImpl
        implements EcheancierService {

    private final EcheanceRepository echeanceRepository;
    private final ContratRepository contratRepository;
    private final UtilisateurRepository utilisateurRepository;


    // =====================================================
    // CALCUL THÉORIQUE DE L'AMORTISSEMENT
    // =====================================================

    @Override
    public List<AmortissementResponse> genererEcheancier(
            DemandeCredit demande,
            String email
    ) {

        if (demande == null) {
            throw new RuntimeException(
                    "Demande introuvable."
            );
        }

        // -------------------------------------------------
        // Vérification des droits
        // -------------------------------------------------

        verifierAccesDemande(
                demande,
                email
        );

        List<AmortissementResponse> echeancier =
                new ArrayList<>();

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

        // -------------------------------------------------
        // Ne jamais créer deux fois les mêmes échéances
        // -------------------------------------------------

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
            Long contratId,
            String email
    ) {

        if (contratId == null) {

            throw new RuntimeException(
                    "L'identifiant du contrat est obligatoire."
            );
        }

        Contrat contrat =
                contratRepository
                        .findById(contratId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Contrat introuvable."
                                )
                        );

        if (contrat.getDemandeCredit() == null) {

            throw new RuntimeException(
                    "La demande associée au contrat est introuvable."
            );
        }

        // -------------------------------------------------
        // Vérification de l'accès
        // -------------------------------------------------

        verifierAccesDemande(
                contrat.getDemandeCredit(),
                email
        );

        List<Echeance> echeances =
                echeanceRepository.findByContratId(
                        contratId
                );

        return convertirListe(
                echeances
        );
    }


    // =====================================================
    // VÉRIFIER L'ACCÈS À UNE DEMANDE
    // =====================================================

    private void verifierAccesDemande(
            DemandeCredit demande,
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new RuntimeException(
                    "Utilisateur connecté introuvable."
            );
        }

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable."
                                )
                        );

        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (utilisateur.getRole() != null
                && "ADMIN".equals(
                utilisateur.getRole().getNom()
        )) {

            return;
        }

        // -------------------------------------------------
        // CLIENT
        // -------------------------------------------------

        if (utilisateur instanceof Client) {

            if (demande.getClient() == null
                    || demande.getClient().getId() == null
                    || !demande.getClient()
                    .getId()
                    .equals(utilisateur.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande n'appartient pas au client connecté."
                );
            }

            return;
        }

        // -------------------------------------------------
        // CONSEILLER
        // -------------------------------------------------

        if (utilisateur instanceof Conseiller) {

            if (demande.getConseiller() == null
                    || demande.getConseiller().getId() == null
                    || !demande.getConseiller()
                    .getId()
                    .equals(utilisateur.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande n'est pas affectée au conseiller connecté."
                );
            }

            return;
        }

        // -------------------------------------------------
        // RESPONSABLE CRÉDIT
        // -------------------------------------------------

        if (utilisateur instanceof ResponsableCredit) {

            if (demande.getResponsable() == null
                    || demande.getResponsable().getId() == null
                    || !demande.getResponsable()
                    .getId()
                    .equals(utilisateur.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande n'est pas affectée au responsable connecté."
                );
            }

            return;
        }

        // -------------------------------------------------
        // Autre utilisateur
        // -------------------------------------------------

        throw new RuntimeException(
                "Accès interdit."
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