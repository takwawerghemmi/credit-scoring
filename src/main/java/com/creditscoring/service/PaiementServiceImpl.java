package com.creditscoring.service;

import com.creditscoring.dto.request.PaiementRequest;
import com.creditscoring.dto.reponse.PaiementResponse;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.Echeance;
import com.creditscoring.entity.Paiement;
import com.creditscoring.enums.StatutEcheance;
import com.creditscoring.enums.StatutPaiement;
import com.creditscoring.repository.ContratRepository;
import com.creditscoring.repository.EcheanceRepository;
import com.creditscoring.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl
        implements PaiementService {

    private final PaiementRepository paiementRepository;
    private final ContratRepository contratRepository;
    private final EcheanceRepository echeanceRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PaiementResponse creerPaiement(
            PaiementRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "Requête de paiement invalide."
            );
        }

        if (request.getEcheanceId() == null) {
            throw new RuntimeException(
                    "L'échéance est obligatoire."
            );
        }

        if (request.getMethodePaiement() == null
                || request.getMethodePaiement().isBlank()) {

            throw new RuntimeException(
                    "La méthode de paiement est obligatoire."
            );
        }

        Echeance echeance =
                echeanceRepository.findById(
                        request.getEcheanceId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Échéance introuvable."
                        )
                );

        Contrat contrat =
                echeance.getContrat();

        if (contrat == null) {
            throw new RuntimeException(
                    "Cette échéance n'est liée à aucun contrat."
            );
        }

        if (contrat.getSignatureClient() == null
                || contrat.getSignatureClient().isBlank()) {

            throw new RuntimeException(
                    "Le contrat doit être signé avant le paiement."
            );
        }

        if (echeance.getStatut()
                == StatutEcheance.PAYEE) {

            throw new RuntimeException(
                    "Cette échéance est déjà payée."
            );
        }

        paiementRepository
                .findByEcheanceId(
                        echeance.getId()
                )
                .ifPresent(p -> {

                    if (p.getStatut()
                            == StatutPaiement.EN_ATTENTE) {

                        throw new RuntimeException(
                                "Un paiement est déjà en attente pour cette échéance."
                        );
                    }

                    if (p.getStatut()
                            == StatutPaiement.EFFECTUE) {

                        throw new RuntimeException(
                                "Cette échéance a déjà été payée."
                        );
                    }
                });

        Paiement paiement =
                new Paiement();

        paiement.setMontant(
                echeance.getMontant()
        );

        paiement.setDatePaiement(
                null
        );

        paiement.setStatut(
                StatutPaiement.EN_ATTENTE
        );

        paiement.setMethodePaiement(
                request.getMethodePaiement()
        );

        paiement.setContrat(
                contrat
        );

        paiement.setEcheance(
                echeance
        );

        paiement.setPenalite(
                echeance.getPenalite()
        );

        paiement.setJoursRetard(
                calculerJoursRetard(
                        echeance
                )
        );

        Paiement sauvegarde =
                paiementRepository.save(
                        paiement
                );

        return convertir(
                sauvegarde
        );
    }

    @Override
    @Transactional
    public PaiementResponse confirmerPaiement(
            Long paiementId
    ) {

        Paiement paiement =
                paiementRepository.findById(
                        paiementId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Paiement introuvable."
                        )
                );

        if (paiement.getStatut()
                == StatutPaiement.EFFECTUE) {

            throw new RuntimeException(
                    "Le paiement est déjà effectué."
            );
        }

        if (paiement.getStatut()
                == StatutPaiement.ECHEC) {

            throw new RuntimeException(
                    "Ce paiement est déjà marqué comme échoué."
            );
        }

        Echeance echeance =
                paiement.getEcheance();

        if (echeance == null) {

            throw new RuntimeException(
                    "L'échéance du paiement est introuvable."
            );
        }

        paiement.setStatut(
                StatutPaiement.EFFECTUE
        );

        paiement.setDatePaiement(
                LocalDate.now()
        );

        echeance.setStatut(
                StatutEcheance.PAYEE
        );

        echeance.setDatePaiement(
                LocalDate.now()
        );

        echeanceRepository.save(
                echeance
        );

        paiementRepository.save(
                paiement
        );

        // =====================================================
        // NOTIFICATION CLIENT
        // =====================================================

        Contrat contrat =
                paiement.getContrat();

        if (contrat != null
                && contrat.getDemandeCredit() != null
                && contrat.getDemandeCredit().getClient() != null) {

            Client client =
                    contrat
                            .getDemandeCredit()
                            .getClient();

            notificationService
                    .creerNotificationAutomatique(
                            client,
                            contrat.getDemandeCredit(),
                            "Paiement effectué",
                            "Votre paiement de "
                                    + paiement.getMontant()
                                    + " TND pour l'échéance #"
                                    + echeance.getNumero()
                                    + " a été effectué avec succès."
                    );
        }

        return convertir(
                paiement
        );
    }

    @Override
    @Transactional
    public PaiementResponse echouerPaiement(
            Long paiementId
    ) {

        Paiement paiement =
                paiementRepository.findById(
                        paiementId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Paiement introuvable."
                        )
                );

        if (paiement.getStatut()
                == StatutPaiement.EFFECTUE) {

            throw new RuntimeException(
                    "Un paiement effectué ne peut pas être marqué comme échoué."
            );
        }

        paiement.setStatut(
                StatutPaiement.ECHEC
        );

        paiement.setDatePaiement(
                null
        );

        paiementRepository.save(
                paiement
        );

        Contrat contrat =
                paiement.getContrat();

        if (contrat != null
                && contrat.getDemandeCredit() != null
                && contrat.getDemandeCredit().getClient() != null) {

            notificationService
                    .creerNotificationAutomatique(
                            contrat
                                    .getDemandeCredit()
                                    .getClient(),
                            contrat.getDemandeCredit(),
                            "Paiement échoué",
                            "Le paiement de "
                                    + paiement.getMontant()
                                    + " TND pour l'échéance #"
                                    + paiement
                                    .getEcheance()
                                    .getNumero()
                                    + " a échoué."
                    );
        }

        return convertir(
                paiement
        );
    }

    @Override
    public List<PaiementResponse> getAllPaiements() {

        return paiementRepository.findAll()
                .stream()
                .map(this::convertir)
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    public PaiementResponse getPaiementById(
            Long id
    ) {

        Paiement paiement =
                paiementRepository.findById(
                        id
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Paiement introuvable."
                        )
                );

        return convertir(
                paiement
        );
    }

    @Override
    public List<PaiementResponse> getPaiementsByContrat(
            Long contratId
    ) {

        if (!contratRepository.existsById(
                contratId
        )) {

            throw new RuntimeException(
                    "Contrat introuvable."
            );
        }

        return paiementRepository
                .findByContratId(
                        contratId
                )
                .stream()
                .map(this::convertir)
                .collect(
                        Collectors.toList()
                );
    }

    @Override
    @Transactional
    public void deletePaiement(
            Long id
    ) {

        if (!paiementRepository.existsById(
                id
        )) {

            throw new RuntimeException(
                    "Paiement introuvable."
            );
        }

        paiementRepository.deleteById(
                id
        );
    }

    private PaiementResponse convertir(
            Paiement paiement
    ) {

        PaiementResponse response =
                new PaiementResponse();

        response.setId(
                paiement.getId()
        );

        response.setMontant(
                paiement.getMontant()
        );

        response.setDatePaiement(
                paiement.getDatePaiement()
        );

        response.setStatut(
                paiement.getStatut()
        );

        response.setMethodePaiement(
                paiement.getMethodePaiement()
        );

        if (paiement.getContrat() != null) {

            response.setContratId(
                    paiement
                            .getContrat()
                            .getId()
            );
        }

        if (paiement.getEcheance() != null) {

            response.setEcheanceId(
                    paiement
                            .getEcheance()
                            .getId()
            );

            response.setNumeroEcheance(
                    paiement
                            .getEcheance()
                            .getNumero()
            );

            response.setDateEcheance(
                    paiement
                            .getEcheance()
                            .getDateEcheance()
            );
        }

        return response;
    }

    private Integer calculerJoursRetard(
            Echeance echeance
    ) {

        if (echeance.getDateEcheance() == null) {
            return 0;
        }

        LocalDate aujourdHui =
                LocalDate.now();

        if (aujourdHui.isAfter(
                echeance.getDateEcheance()
        )) {

            return (int)
                    java.time.temporal.ChronoUnit
                            .DAYS
                            .between(
                                    echeance.getDateEcheance(),
                                    aujourdHui
                            );
        }

        return 0;
    }
}