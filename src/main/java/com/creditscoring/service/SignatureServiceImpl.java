package com.creditscoring.service;

import com.creditscoring.entity.Contrat;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.repository.ContratRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SignatureServiceImpl
        implements SignatureService {

    private final ContratRepository contratRepository;
    private final EcheancierService echeancierService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public String signerContrat(
            Contrat contrat
    ) {

        if (contrat == null) {

            throw new RuntimeException(
                    "Contrat introuvable."
            );
        }

        if (contrat.getSignatureClient() != null
                && !contrat.getSignatureClient().isBlank()) {

            throw new RuntimeException(
                    "Le contrat est déjà signé."
            );
        }

        String signature =
                "SIG-"
                        + contrat.getId()
                        + "-"
                        + System.currentTimeMillis();

        LocalDateTime dateSignature =
                LocalDateTime.now();

        contrat.setSignatureClient(
                signature
        );

        contrat.setDateSignature(
                dateSignature
        );

        DemandeCredit demande =
                contrat.getDemandeCredit();

        if (demande != null) {

            demande.setStatut(
                    StatutDemande.CONTRAT_SIGNE
            );
        }

        contratRepository.save(
                contrat
        );

        // =====================================================
        // CRÉER ÉCHÉANCES
        // =====================================================

        echeancierService.creerEcheances(
                contrat
        );

        // =====================================================
        // NOTIFICATION CLIENT
        // =====================================================

        if (demande != null
                && demande.getClient() != null) {

            notificationService
                    .creerNotificationAutomatique(
                            demande.getClient(),
                            demande,
                            "Contrat signé",
                            "Votre contrat "
                                    + contrat.getNumeroContrat()
                                    + " a été signé avec succès. Votre échéancier est maintenant disponible."
                    );
        }

        // =====================================================
        // NOTIFICATION DIRECTEUR
        // =====================================================

        if (contrat.getUtilisateur() != null) {

            notificationService
                    .creerNotificationAutomatique(
                            contrat.getUtilisateur(),
                            demande,
                            "Contrat signé par le client",
                            "Le contrat "
                                    + contrat.getNumeroContrat()
                                    + " a été signé par le client."
                    );
        }

        return
                "Contrat signé avec succès.\n"
                        + "Signature : "
                        + signature
                        + "\n"
                        + "Date : "
                        + dateSignature
                        + "\n"
                        + "Échéancier généré avec succès.";
    }
}