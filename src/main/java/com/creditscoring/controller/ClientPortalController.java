package com.creditscoring.controller;

import com.creditscoring.entity.Client;
import com.creditscoring.repository.ClientRepository;
import com.creditscoring.service.ClientPortalService;
import com.creditscoring.dto.reponse.ClientDashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientPortalController {

    private final ClientRepository clientRepository;
    private final ClientPortalService clientPortalService;

    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardDTO> getDashboard(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                clientPortalService.getDashboard(email)
        );
    }

    @GetMapping("/me")
    public ResponseEntity<Client> getCurrentClient(Authentication authentication) {

        String email = authentication.getName();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Client introuvable"));

        return ResponseEntity.ok(client);
    }
}
