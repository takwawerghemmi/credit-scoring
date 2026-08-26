package com.creditscoring.service;

import com.creditscoring.dto.reponse.NotificationResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Utilisateur;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getAllNotifications();

    NotificationResponse getNotificationById(Long id);

    List<NotificationResponse> getNotificationsByUtilisateur(
            Long utilisateurId
    );

    void supprimerNotification(Long id);

    void creerNotificationAutomatique(
            Utilisateur utilisateur,
            DemandeCredit demandeCredit,
            String titre,
            String message
    );
}