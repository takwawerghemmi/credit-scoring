package com.creditscoring.controller;

import com.creditscoring.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email-test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<String> envoyerEmailTest(
            @RequestParam String destinataire
    ) {

        emailService.envoyerEmail(
                destinataire,
                "CreditNova - Test Email",
                "Bonjour,\n\n"
                        + "Ceci est un email de test envoyé par CreditNova.\n\n"
                        + "Le service d'envoi d'emails fonctionne correctement.\n\n"
                        + "CreditNova Bank"
        );

        return ResponseEntity.ok(
                "Email envoyé avec succès à : "
                        + destinataire
        );
    }
}
