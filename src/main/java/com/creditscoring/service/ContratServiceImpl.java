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
    private final NotificationService notificationService;

    // =========================================================
    // CREATION MANUELLE
    // =========================================================

    @Override
    @Transactional
    public Contrat creerContrat(
            ContratRequest request
    ) {

        if (request == null) {
            throw new RuntimeException(
                    "La demande de création du contrat est obligatoire."
            );
        }

        Utilisateur utilisateur =
                utilisateurRepository.findById(
                        request.getUtilisateurId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable."
                        )
                );

        DemandeCredit demande =
                demandeCreditRepository.findById(
                        request.getDemandeCreditId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable."
                        )
                );

        if (demande.getStatut() == null
                || !"APPROUVEE".equals(
                demande.getStatut().name()
        )) {

            throw new RuntimeException(
                    "La demande doit être APPROUVEE."
            );
        }

        if (contratRepository
                .findByDemandeCreditId(
                        demande.getId()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Un contrat existe déjà pour cette demande."
            );
        }

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

    // =========================================================
    // CREATION AUTOMATIQUE
    // RESPONSABLE = DECIDEUR FINAL
    // =========================================================

    @Override
    @Transactional
    public Contrat creerContratAutomatiquement(
            DemandeCredit demande,
            Utilisateur responsable
    ) {

        if (demande == null) {
            throw new RuntimeException(
                    "Demande introuvable."
            );
        }

        if (responsable == null) {
            throw new RuntimeException(
                    "Responsable introuvable."
            );
        }

        // =====================================================
        // LA DEMANDE DOIT ETRE APPROUVEE
        // =====================================================

        if (demande.getStatut() == null
                || !"APPROUVEE".equals(
                demande.getStatut().name()
        )) {

            throw new RuntimeException(
                    "La demande doit être APPROUVEE avant la création du contrat."
            );
        }

        // =====================================================
        // LE RESPONSABLE DOIT ETRE AFFECTE A LA DEMANDE
        // =====================================================

        if (demande.getResponsable() == null
                || !demande.getResponsable()
                .getId()
                .equals(
                        responsable.getId()
                )) {

            throw new RuntimeException(
                    "Ce Responsable n'est pas affecté à cette demande."
            );
        }

        // =====================================================
        // UN SEUL CONTRAT PAR DEMANDE
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
        // DATE DEBUT
        // =====================================================

        LocalDate dateDebut =
                LocalDate.now();

        // =====================================================
        // DATE FIN
        // =====================================================

        LocalDate dateFin =
                dateDebut.plusMonths(
                        demande.getDuree()
                );

        // =====================================================
        // MONTANT ACCORDE
        // =====================================================

        Double montant =
                demande.getMontantAccorde() != null
                        ? demande.getMontantAccorde()
                        : demande.getMontant();

        // =====================================================
        // TAUX
        // =====================================================

        Double taux =
                demande.getTauxInteret();

        // =====================================================
        // MENSUALITE / COUT TOTAL
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
        // NUMERO CONTRAT
        // =====================================================

        String numeroContrat =
                "CN-"
                        + LocalDate.now().getYear()
                        + "-"
                        + demande.getId()
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(
                                0,
                                6
                        )
                        .toUpperCase();

        // =====================================================
        // CREATION CONTRAT
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

                        // IMPORTANT:
                        // Le contrat appartient au
                        // Responsable qui a pris la
                        // décision finale
                        .utilisateur(
                                responsable
                        )

                        .demandeCredit(
                                demande
                        )

                        .build();

        return contratRepository.save(
                contrat
        );
    }

    // =========================================================
    // TOUS LES CONTRATS
    // =========================================================

    @Override
    public List<Contrat> getAllContrats() {

        return contratRepository.findAll();
    }

    // =========================================================
    // CONTRATS DU RESPONSABLE CONNECTE
    // =========================================================

    @Override
    public List<Contrat> getContratsDuResponsable(
            String email
    ) {

        Utilisateur responsable =
                utilisateurRepository.findByEmail(
                        email
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Responsable connecté introuvable."
                        )
                );

        return contratRepository
                .findAll()
                .stream()
                .filter(contrat ->
                        contrat.getUtilisateur() != null
                                && contrat.getUtilisateur()
                                .getId()
                                .equals(
                                        responsable.getId()
                                )
                )
                .toList();
    }

    // =========================================================
    // CONTRATS DU CLIENT CONNECTE
    // =========================================================

    @Override
    public List<Contrat> getContratsDuClient(
            String email
    ) {

        Utilisateur client =
                utilisateurRepository.findByEmail(
                        email
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Client connecté introuvable."
                        )
                );

        return contratRepository
                .findAll()
                .stream()
                .filter(contrat ->
                        contrat.getDemandeCredit() != null
                                && contrat.getDemandeCredit()
                                .getClient() != null
                                && contrat.getDemandeCredit()
                                .getClient()
                                .getId()
                                .equals(
                                        client.getId()
                                )
                )
                .toList();
    }

    // =========================================================
    // CONTRAT PAR ID
    // =========================================================

    @Override
    public Contrat getContratById(
            Long id
    ) {

        return contratRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Contrat introuvable."
                        )
                );
    }

    // =========================================================
    // CONTRAT PAR DEMANDE
    // =========================================================

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

    // =========================================================
    // ENVOYER CONTRAT AU CLIENT
    // =========================================================

    @Override
    @Transactional
    public String envoyerContratAuClient(
            Long contratId,
            String emailResponsable
    ) {

        Contrat contrat =
                getContratById(
                        contratId
                );

        // =====================================================
        // RESPONSABLE CONNECTE
        // =====================================================

        Utilisateur responsable =
                utilisateurRepository.findByEmail(
                        emailResponsable
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Responsable connecté introuvable."
                        )
                );

        // =====================================================
        // VERIFIER PROPRIETE DU CONTRAT
        // =====================================================

        if (contrat.getUtilisateur() == null
                || !contrat.getUtilisateur()
                .getId()
                .equals(
                        responsable.getId()
                )) {

            throw new RuntimeException(
                    "Accès interdit : ce contrat appartient à un autre Responsable."
            );
        }

        // =====================================================
        // VERIFIER CLIENT
        // =====================================================

        if (contrat.getDemandeCredit() == null
                || contrat.getDemandeCredit()
                .getClient() == null) {

            throw new RuntimeException(
                    "Client associé au contrat introuvable."
            );
        }

        Utilisateur client =
                contrat.getDemandeCredit()
                        .getClient();

        // =====================================================
        // NOTIFICATION CLIENT
        // =====================================================

        notificationService
                .creerNotificationAutomatique(
                        client,
                        contrat.getDemandeCredit(),
                        "Contrat disponible",
                        "Votre contrat "
                                + contrat.getNumeroContrat()
                                + " est maintenant disponible dans votre espace client. "
                                + "Vous pouvez le consulter, le télécharger et le signer."
                );

        return "Contrat envoyé au client avec succès.";
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    @Transactional
    public Contrat updateContrat(
            Long id,
            ContratRequest request
    ) {

        Contrat contrat =
                getContratById(
                        id
                );

        Utilisateur utilisateur =
                utilisateurRepository.findById(
                        request.getUtilisateurId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable."
                        )
                );

        DemandeCredit demande =
                demandeCreditRepository.findById(
                        request.getDemandeCreditId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable."
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

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void deleteContrat(
            Long id
    ) {

        contratRepository.deleteById(
                id
        );
    }
}