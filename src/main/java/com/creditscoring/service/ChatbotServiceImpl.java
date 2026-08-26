package com.creditscoring.service;

import org.springframework.stereotype.Service;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Override
    public String repondre(String message) {

        message = message.toLowerCase();

        if (message.contains("bonjour") || message.contains("salut")) {
            return "Bonjour ! Comment puis-je vous aider ?";
        }

        if (message.contains("crédit")) {
            return "Nous proposons des crédits immobilier, automobile et consommation.";
        }

        if (message.contains("simulation")) {
            return "Vous pouvez effectuer une simulation de crédit depuis le menu Simulation.";
        }

        if (message.contains("documents")) {
            return "Les documents demandés sont : CIN, justificatif de revenu et relevé bancaire.";
        }

        if (message.contains("statut")) {
            return "Vous pouvez consulter le statut de votre demande dans votre espace client.";
        }

        if (message.contains("fraude")) {
            return "Notre système analyse automatiquement les demandes afin de détecter les risques de fraude.";
        }

        if (message.contains("merci")) {
            return "Avec plaisir !";
        }

        return "Je n'ai pas compris votre question. Reformulez votre demande.";
    }
}