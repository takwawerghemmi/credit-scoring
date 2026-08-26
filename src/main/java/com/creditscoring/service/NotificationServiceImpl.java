package com.creditscoring.service;

import com.creditscoring.dto.reponse.NotificationResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Notification;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.CanalNotification;
import com.creditscoring.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public List<NotificationResponse> getAllNotifications() {

        return notificationRepository.findAll()
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse getNotificationById(Long id) {

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification introuvable"
                                )
                        );

        return convertir(notification);
    }

    @Override
    public List<NotificationResponse>
    getNotificationsByUtilisateur(Long utilisateurId) {

        return notificationRepository
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerNotification(Long id) {

        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException(
                    "Notification introuvable"
            );
        }

        notificationRepository.deleteById(id);
    }

    // =====================================================
    // NOTIFICATION AUTOMATIQUE
    // IN-APP + EMAIL
    // =====================================================

    @Override
    @Transactional
    public void creerNotificationAutomatique(
            Utilisateur utilisateur,
            DemandeCredit demandeCredit,
            String titre,
            String message
    ) {

        if (utilisateur == null) {
            return;
        }

        // =================================================
        // 1. NOTIFICATION IN-APP
        // =================================================

        Notification notification =
                Notification.builder()
                        .titre(titre)
                        .message(message)
                        .dateEnvoi(LocalDateTime.now())
                        .lu(false)
                        .canal(CanalNotification.IN_APP)
                        .utilisateur(utilisateur)
                        .demandeCredit(demandeCredit)
                        .build();

        notificationRepository.save(notification);

        // =================================================
        // 2. EMAIL
        // =================================================

        if (utilisateur.getEmail() != null
                && !utilisateur.getEmail().isBlank()) {

            try {

                emailService.envoyerEmail(
                        utilisateur.getEmail(),
                        "CreditNova - " + titre,
                        message
                );

                System.out.println(
                        "Email envoyé à : "
                                + utilisateur.getEmail()
                );

            } catch (Exception e) {

                // On ne bloque pas la notification IN_APP
                // si l'envoi email échoue.

                System.err.println(
                        "Erreur lors de l'envoi email à "
                                + utilisateur.getEmail()
                );

                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // CONVERSION
    // =====================================================

    private NotificationResponse convertir(
            Notification notification
    ) {

        NotificationResponse response =
                new NotificationResponse();

        response.setId(
                notification.getId()
        );

        response.setTitre(
                notification.getTitre()
        );

        response.setMessage(
                notification.getMessage()
        );

        response.setDateEnvoi(
                notification.getDateEnvoi()
        );

        response.setLu(
                notification.isLu()
        );

        if (notification.getUtilisateur() != null) {

            response.setUtilisateurId(
                    notification
                            .getUtilisateur()
                            .getId()
            );
        }

        return response;
    }
}