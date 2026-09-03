package com.creditscoring.service;

import com.creditscoring.entity.ResponsableCredit;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.repository.ResponsableCreditRepository;
import com.creditscoring.repository.ConseillerRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AffectationServiceImpl implements AffectationService {

    private final DemandeCreditRepository demandeCreditRepository;
    private final ConseillerRepository conseillerRepository;
    private final ResponsableCreditRepository responsableCreditRepository;
    private final NotificationService notificationService;

    // =========================================================
    // AFFECTER UNE DEMANDE
    // ADMIN → CONSEILLER + RESPONSABLE
    // =========================================================

    @Override
    @Transactional
    public String affecterDemande(
            Long demandeId,
            Long conseillerId,
            Long responsableId
    ) {

        // =====================================================
        // Récupérer la demande
        // =====================================================

        DemandeCredit demande =
                demandeCreditRepository.findById(
                        demandeId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        // =====================================================
        // Récupérer le Conseiller
        // =====================================================

        Conseiller conseiller =
                conseillerRepository.findById(
                        conseillerId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Conseiller introuvable"
                        )
                );

        // =====================================================
        // Récupérer le Responsable Crédit
        // =====================================================

        ResponsableCredit responsable =
                responsableCreditRepository.findById(
                        responsableId
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Responsable Crédit introuvable"
                        )
                );

        // =====================================================
        // Affecter les deux acteurs
        // =====================================================

        demande.setConseiller(
                conseiller
        );

        demande.setResponsable(
                responsable
        );

        demandeCreditRepository.save(
                demande
        );

        // =====================================================
        // NOTIFICATION CONSEILLER
        // =====================================================

        notificationService.creerNotificationAutomatique(
                conseiller,
                demande,
                "Nouvelle demande affectée",
                "La demande de crédit #"
                        + demandeId
                        + " vous a été affectée. "
                        + "Veuillez procéder à son analyse."
        );

        // =====================================================
        // NOTIFICATION RESPONSABLE
        // =====================================================

        notificationService.creerNotificationAutomatique(
                responsable,
                demande,
                "Nouvelle demande affectée",
                "La demande de crédit #"
                        + demandeId
                        + " vous a été affectée en tant que Responsable Crédit."
                        + " Vous serez chargé de la validation et de la décision finale."
        );

        // =====================================================
        // Réponse
        // =====================================================

        return "La demande "
                + demandeId
                + " a été affectée au conseiller "
                + conseiller.getNom()
                + " "
                + conseiller.getPrenom()
                + " et au responsable "
                + responsable.getNom()
                + " "
                + responsable.getPrenom();
    }
}