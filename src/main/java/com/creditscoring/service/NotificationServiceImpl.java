package com.creditscoring.service;
import com.creditscoring.repository.UtilisateurRepository;
import com.creditscoring.dto.reponse.NotificationResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Notification;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.enums.CanalNotification;
import com.creditscoring.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final UtilisateurRepository utilisateurRepository;    // =====================================================
    // GET ALL
    // =====================================================

    @Override
    public List<NotificationResponse> getAllNotifications() {

        Utilisateur utilisateurConnecte = getUtilisateurConnecte();

        return notificationRepository
                .findByUtilisateurId(utilisateurConnecte.getId())
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // =====================================================
    // GET BY ID
    // =====================================================

    @Override
    public NotificationResponse getNotificationById(Long id) {

        Utilisateur utilisateurConnecte = getUtilisateurConnecte();

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification introuvable"
                                )
                        );

        // Sécurité : la notification doit appartenir
        // à l'utilisateur connecté
        if (notification.getUtilisateur() == null
                || !notification.getUtilisateur().getId()
                .equals(utilisateurConnecte.getId())) {

            throw new RuntimeException(
                    "Accès interdit à cette notification."
            );
        }

        return convertir(notification);
    }

    // =====================================================
    // GET BY USER
    // =====================================================

    @Override
    public List<NotificationResponse> getNotificationsByUtilisateur(
            Long utilisateurId) {

        Utilisateur utilisateurConnecte = getUtilisateurConnecte();

        // Un utilisateur ne peut consulter que ses propres notifications.
        if (!utilisateurConnecte.getId().equals(utilisateurId)) {
            throw new RuntimeException(
                    "Accès interdit aux notifications d'un autre utilisateur."
            );
        }

        return notificationRepository
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::convertir)
                .collect(Collectors.toList());
    }

    // =====================================================
    // DELETE
    // =====================================================

    @Override
    public void supprimerNotification(Long id) {

        Utilisateur utilisateurConnecte = getUtilisateurConnecte();

        Notification notification =
                notificationRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notification introuvable"
                                )
                        );

        if (notification.getUtilisateur() == null
                || !notification.getUtilisateur().getId()
                .equals(utilisateurConnecte.getId())) {

            throw new RuntimeException(
                    "Accès interdit à cette notification."
            );
        }

        notificationRepository.delete(notification);
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

                System.err.println(
                        "Erreur lors de l'envoi email à "
                                + utilisateur.getEmail()
                );

                e.printStackTrace();
            }
        }
    }

    // =====================================================
    // UTILISATEUR CONNECTÉ
    // =====================================================
    private Utilisateur getUtilisateurConnecte() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {

            throw new RuntimeException(
                    "Utilisateur non authentifié."
            );
        }

        String email = authentication.getName();

        return utilisateurRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur connecté introuvable."
                        )
                );
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