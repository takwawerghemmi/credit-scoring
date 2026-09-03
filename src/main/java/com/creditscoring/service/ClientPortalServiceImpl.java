package com.creditscoring.service;

import com.creditscoring.dto.reponse.ClientDashboardDTO;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.CreditScore;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Notification;
import com.creditscoring.repository.ClientRepository;
import com.creditscoring.repository.CreditScoreRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.creditscoring.dto.reponse.ClientRecentDemandeDTO;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientPortalServiceImpl implements ClientPortalService {

    private final ClientRepository clientRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public ClientDashboardDTO getDashboard(String email) {

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Client introuvable pour l'utilisateur authentifié"));

        Long clientId = client.getId();

        List<DemandeCredit> demandes =
                demandeCreditRepository.findByClientId(clientId);

        Long totalApplications =
                demandeCreditRepository.nombreDemandesClient(clientId);

        Long pendingApplications =
                demandeCreditRepository.nombreDemandesEnAnalyseClient(clientId);

        Long approvedApplications =
                demandeCreditRepository.nombreDemandesApprouveesClient(clientId);

        Long rejectedApplications =
                demandeCreditRepository.nombreDemandesRefuseesClient(clientId);

        Double totalRequestedAmount =
                demandeCreditRepository.montantTotalDemandeClient(clientId);

        Double totalApprovedAmount =
                demandeCreditRepository.montantTotalAccordeClient(clientId);

        List<Notification> notifications =
                notificationRepository.findByUtilisateurId(clientId);

        long unreadNotifications = notifications.stream()
                .filter(notification -> !notification.isLu())
                .count();

        Double creditScore = null;
        String niveauRisque = "NON_CALCULÉ";

        for (DemandeCredit demande : demandes) {

            List<CreditScore> scores = creditScoreRepository.findAll();

            for (CreditScore score : scores) {

                if (score.getDemandeCredit() != null
                        && score.getDemandeCredit().getId().equals(demande.getId())) {

                    creditScore = score.getScore();

                    if (score.getNiveauRisque() != null) {
                        niveauRisque = score.getNiveauRisque().name();
                    }

                    break;
                }
            }

            if (creditScore != null) {
                break;
            }
        }

        Map<String, Long> distribution = new LinkedHashMap<>();

        demandes.forEach(demande -> {
            String status = demande.getStatut() != null
                    ? demande.getStatut().name()
                    : "INCONNU";

            distribution.put(
                    status,
                    distribution.getOrDefault(status, 0L) + 1
            );
        });

        List<ClientRecentDemandeDTO> recentApplications =
                demandes.stream()
                        .sorted((a, b) -> {
                            if (a.getDateDemande() == null) return 1;
                            if (b.getDateDemande() == null) return -1;

                            return b.getDateDemande()
                                    .compareTo(a.getDateDemande());
                        })
                        .limit(5)
                        .map(d -> ClientRecentDemandeDTO.builder()
                                .id(d.getId())
                                .montant(d.getMontant())
                                .duree(d.getDuree())
                                .typeCredit(d.getTypeCredit())
                                .statut(
                                        d.getStatut() != null
                                                ? d.getStatut().name()
                                                : null
                                )
                                .revenuMensuel(d.getRevenuMensuel())
                                .chargesMensuelles(d.getChargesMensuelles())
                                .dateDemande(d.getDateDemande())
                                .montantAccorde(d.getMontantAccorde())
                                .motifRefus(d.getMotifRefus())
                                .commentaireAnalyse(d.getCommentaireAnalyse())
                                .build())
                        .toList();

        return ClientDashboardDTO.builder()
                .clientId(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .creditScore(creditScore)
                .niveauRisque(niveauRisque)
                .totalApplications(totalApplications != null ? totalApplications : 0L)
                .pendingApplications(pendingApplications != null ? pendingApplications : 0L)
                .approvedApplications(approvedApplications != null ? approvedApplications : 0L)
                .rejectedApplications(rejectedApplications != null ? rejectedApplications : 0L)
                .totalRequestedAmount(totalRequestedAmount != null ? totalRequestedAmount : 0.0)
                .totalApprovedAmount(totalApprovedAmount != null ? totalApprovedAmount : 0.0)
                .unreadNotifications(unreadNotifications)
                .recentApplications(recentApplications)
                .applicationStatusDistribution(distribution)
                .build();
    }
}
