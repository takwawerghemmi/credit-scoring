package com.creditscoring.service;

import com.creditscoring.dto.request.ContratRequest;
import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.StatutContrat;
import com.creditscoring.repository.ContratRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratServiceImpl
        implements ContratService {

    private final ContratRepository contratRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeCreditRepository demandeCreditRepository;

    @Override
    @Transactional
    public Contrat creerContrat(
            ContratRequest request
    ) {

        Utilisateur utilisateur =
                utilisateurRepository.findById(
                        request.getUtilisateurId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );

        DemandeCredit demande =
                demandeCreditRepository.findById(
                        request.getDemandeCreditId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        Contrat contrat =
                new Contrat();

        contrat.setNumeroContrat(
                request.getNumeroContrat()
        );

        contrat.setDateDebut(
                request.getDateDebut()
        );

        contrat.setDateFin(
                request.getDateFin()
        );

        contrat.setMontant(
                request.getMontant()
        );

        contrat.setUtilisateur(
                utilisateur
        );

        contrat.setDemandeCredit(
                demande
        );

        return contratRepository.save(
                contrat
        );
    }

    @Override
    @Transactional
    public Contrat creerContratAutomatiquement(
            DemandeCredit demande,
            Utilisateur directeur
    ) {

        if (demande == null) {

            throw new RuntimeException(
                    "Demande introuvable."
            );
        }

        if (directeur == null) {

            throw new RuntimeException(
                    "Directeur introuvable."
            );
        }

        // =====================================================
        // Ne pas créer deux contrats
        // =====================================================

        if (contratRepository
                .findByDemandeCreditId(
                        demande.getId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Un contrat existe déjà pour cette demande."
            );
        }

        // =====================================================
        // Dates
        // =====================================================

        LocalDate dateDebut =
                LocalDate.now();

        LocalDate dateFin =
                dateDebut.plusMonths(
                        demande.getDuree()
                );

        // =====================================================
        // Montant
        // =====================================================

        Double montant =
                demande.getMontantAccorde() != null
                        ? demande.getMontantAccorde()
                        : demande.getMontant();

        // =====================================================
        // Taux
        // =====================================================

        Double taux =
                demande.getTauxInteret();

        // =====================================================
        // Mensualité / coût total
        // =====================================================

        Double mensualite = null;
        Double coutTotal = null;

        if (montant != null
                && demande.getDuree() != null
                && demande.getDuree() > 0) {

            mensualite =
                    montant / demande.getDuree();

            if (taux != null) {

                double interets =
                        montant
                                * (taux / 100.0)
                                * (demande.getDuree() / 12.0);

                coutTotal =
                        montant + interets;

            } else {

                coutTotal =
                        montant;
            }
        }

        // =====================================================
        // Numéro contrat
        // =====================================================

        String numeroContrat =
                "CN-"
                        + LocalDate.now().getYear()
                        + "-"
                        + demande.getId()
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase();

        // =====================================================
        // Créer le contrat
        // =====================================================

        Contrat contrat =
                Contrat.builder()
                        .numeroContrat(
                                numeroContrat
                        )
                        .dateDebut(
                                dateDebut
                        )
                        .dateFin(
                                dateFin
                        )
                        .montant(
                                montant
                        )
                        .tauxInteret(
                                taux
                        )
                        .mensualite(
                                mensualite
                        )
                        .coutTotal(
                                coutTotal
                        )
                        .statut(
                                StatutContrat.ACTIF
                        )

                        // IMPORTANT :
                        // directeur qui a pris la décision
                        .utilisateur(
                                directeur
                        )

                        .demandeCredit(
                                demande
                        )
                        .build();

        return contratRepository.save(
                contrat
        );
    }

    @Override
    public List<Contrat> getAllContrats() {

        return contratRepository.findAll();
    }

    @Override
    public Contrat getContratById(
            Long id
    ) {

        return contratRepository.findById(
                id
        ).orElseThrow(() ->
                new RuntimeException(
                        "Contrat introuvable"
                )
        );
    }

    @Override
    public Contrat getContratByDemandeId(
            Long demandeId
    ) {

        return contratRepository
                .findByDemandeCreditId(
                        demandeId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contrat introuvable pour cette demande."
                        )
                );
    }

    @Override
    @Transactional
    public Contrat updateContrat(
            Long id,
            ContratRequest request
    ) {

        Contrat contrat =
                getContratById(id);

        Utilisateur utilisateur =
                utilisateurRepository.findById(
                        request.getUtilisateurId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );

        DemandeCredit demande =
                demandeCreditRepository.findById(
                        request.getDemandeCreditId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        contrat.setNumeroContrat(
                request.getNumeroContrat()
        );

        contrat.setDateDebut(
                request.getDateDebut()
        );

        contrat.setDateFin(
                request.getDateFin()
        );

        contrat.setMontant(
                request.getMontant()
        );

        contrat.setUtilisateur(
                utilisateur
        );

        contrat.setDemandeCredit(
                demande
        );

        return contratRepository.save(
                contrat
        );
    }

    @Override
    public void deleteContrat(
            Long id
    ) {

        contratRepository.deleteById(
                id
        );
    }
}