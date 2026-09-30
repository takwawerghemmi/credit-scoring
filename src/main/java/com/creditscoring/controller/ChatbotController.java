package com.creditscoring.controller;

import com.creditscoring.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public String discuter(
            @RequestBody String message,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return "Utilisateur non authentifié.";
        }

        return chatbotService.repondre(
                message,
                authentication.getName()
        );
    }
}