package com.creditscoring.controller;

import com.creditscoring.dto.reponse.NotificationResponse;
import com.creditscoring.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @GetMapping("/utilisateur/{utilisateurId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUtilisateur(
            @PathVariable Long utilisateurId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUtilisateur(utilisateurId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> supprimerNotification(@PathVariable Long id) {
        notificationService.supprimerNotification(id);
        return ResponseEntity.ok("Notification supprimée avec succès.");
    }
}