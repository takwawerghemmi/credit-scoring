package com.creditscoring.service;

import com.creditscoring.entity.Historique;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.HistoriqueRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final HistoriqueRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrerAction(
            String utilisateur,
            String action,
            String methode,
            String details
    ) {

        Utilisateur user = null;

        if (utilisateur != null
                && !"ANONYMOUS".equals(utilisateur)) {

            user = utilisateurRepository
                    .findByEmail(utilisateur)
                    .orElse(null);
        }

        Historique historique =
                Historique.builder()
                        .action(action)
                        .dateAction(LocalDateTime.now())
                        .description(details)
                        .entite(action)
                        .entiteId(null)
                        .ancienneValeur(null)
                        .nouvelleValeur(null)
                        .utilisateur(user)
                        .build();

        historiqueRepository.save(historique);

        log.info(
                "AUDIT PERSISTÉ | utilisateur={} | action={} | methode={} | details={}",
                utilisateur,
                action,
                methode,
                details
        );
    }
}