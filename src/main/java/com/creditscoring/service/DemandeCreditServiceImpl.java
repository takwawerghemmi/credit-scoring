package com.creditscoring.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import com.creditscoring.dto.request.DemandeCreditRequest;
import com.creditscoring.dto.reponse.DemandeCreditResponse;
import com.creditscoring.entity.Banque;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Conseiller;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.mapper.DemandeCreditMapper;
import com.creditscoring.repository.BanqueRepository;
import com.creditscoring.repository.ClientRepository;
import com.creditscoring.repository.ConseillerRepository;
import com.creditscoring.repository.DemandeCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeCreditServiceImpl
        implements DemandeCreditService {

    private final DemandeCreditRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final ConseillerRepository conseillerRepository;
    private final BanqueRepository banqueRepository;
    private final NotificationService notificationService;

    @Transactional
    @CacheEvict(
            value = "dashboard",
            allEntries = true
    )
    @Override
    public DemandeCreditResponse creer(
            DemandeCreditRequest request,
            String emailUtilisateur
    ) {

        Client client =
                clientRepository.findByEmail(
                        emailUtilisateur
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Client connecté introuvable"
                        )
                );

        Conseiller conseiller =
                conseillerRepository.findById(
                        request.getConseillerId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Conseiller introuvable"
                        )
                );

        Banque banque =
                banqueRepository.findById(
                        request.getBanqueId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Banque introuvable"
                        )
                );

        DemandeCredit demande =
                DemandeCredit.builder()
                        .montant(
                                request.getMontant()
                        )
                        .duree(
                                request.getDuree()
                        )
                        .typeCredit(
                                request.getTypeCredit()
                        )
                        .revenuMensuel(
                                request.getRevenuMensuel()
                        )
                        .chargesMensuelles(
                                request.getChargesMensuelles()
                        )
                        .statut(
                                StatutDemande.EN_ATTENTE
                        )
                        .client(client)
                        .conseiller(conseiller)
                        .banque(banque)
                        .build();

        demandeRepository.save(
                demande
        );

        // =====================================================
        // NOTIFICATION CONSEILLER
        // =====================================================

        notificationService.creerNotificationAutomatique(
                conseiller,
                demande,
                "Nouvelle demande de crédit",
                "La demande de crédit #"
                        + demande.getId()
                        + " du client "
                        + client.getPrenom()
                        + " "
                        + client.getNom()
                        + " est en attente d'analyse."
        );

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    @Transactional
    @CacheEvict(
            value = "dashboard",
            allEntries = true
    )
    @Override
    public DemandeCreditResponse modifier(
            Long id,
            DemandeCreditRequest request
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        id
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        Client client =
                demande.getClient();

        if (client == null) {
            throw new RuntimeException(
                    "Client de la demande introuvable"
            );
        }

        Conseiller conseiller =
                conseillerRepository.findById(
                        request.getConseillerId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Conseiller introuvable"
                        )
                );

        Banque banque =
                banqueRepository.findById(
                        request.getBanqueId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Banque introuvable"
                        )
                );

        demande.setMontant(
                request.getMontant()
        );

        demande.setDuree(
                request.getDuree()
        );

        demande.setTypeCredit(
                request.getTypeCredit()
        );

        demande.setRevenuMensuel(
                request.getRevenuMensuel()
        );

        demande.setChargesMensuelles(
                request.getChargesMensuelles()
        );

        demande.setClient(client);
        demande.setConseiller(conseiller);
        demande.setBanque(banque);

        demandeRepository.save(
                demande
        );

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    @Override
    public DemandeCreditResponse modifierPourUtilisateur(
            Long id,
            DemandeCreditRequest request,
            String emailUtilisateur,
            String role
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        id
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        if ("ROLE_CLIENT".equals(role)) {

            Client client =
                    clientRepository.findByEmail(
                            emailUtilisateur
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Client connecté introuvable"
                            )
                    );

            if (demande.getClient() == null
                    || !demande
                    .getClient()
                    .getId()
                    .equals(client.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande ne vous appartient pas"
                );
            }
        }

        return modifier(
                id,
                request
        );
    }

    @Override
    public DemandeCreditResponse trouverParId(
            Long id
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        id
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    @Override
    public DemandeCreditResponse trouverParIdPourUtilisateur(
            Long id,
            String emailUtilisateur,
            String role
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        id
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        if ("ROLE_CONSEILLER".equals(role)) {
            return DemandeCreditMapper.toResponse(
                    demande
            );
        }

        Client client =
                clientRepository.findByEmail(
                        emailUtilisateur
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Client connecté introuvable"
                        )
                );

        if (demande.getClient() == null
                || !demande
                .getClient()
                .getId()
                .equals(client.getId())) {

            throw new RuntimeException(
                    "Accès interdit : cette demande ne vous appartient pas"
            );
        }

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    @Override
    public List<DemandeCreditResponse> afficherToutes() {

        return demandeRepository.findAll()
                .stream()
                .map(
                        DemandeCreditMapper::toResponse
                )
                .toList();
    }

    @Override
    public List<DemandeCreditResponse> afficherMesDemandes(
            String emailUtilisateur
    ) {

        Client client =
                clientRepository.findByEmail(
                        emailUtilisateur
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Client connecté introuvable"
                        )
                );

        return demandeRepository
                .findByClientId(
                        client.getId()
                )
                .stream()
                .map(
                        DemandeCreditMapper::toResponse
                )
                .toList();
    }

    @Override
    public List<DemandeCreditResponse> afficherParClient(
            Long clientId
    ) {

        return demandeRepository
                .findByClientId(clientId)
                .stream()
                .map(
                        DemandeCreditMapper::toResponse
                )
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(
            value = "dashboard",
            allEntries = true
    )
    public void supprimerPourUtilisateur(
            Long id,
            String emailUtilisateur,
            String role
    ) {

        DemandeCredit demande =
                demandeRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        if ("ROLE_CLIENT".equals(role)) {

            Client client =
                    clientRepository.findByEmail(
                            emailUtilisateur
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Client connecté introuvable"
                            )
                    );

            if (demande.getClient() == null
                    || !demande
                    .getClient()
                    .getId()
                    .equals(client.getId())) {

                throw new RuntimeException(
                        "Accès interdit : cette demande ne vous appartient pas"
                );
            }
        }

        demandeRepository.delete(
                demande
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            value = "dashboard",
            allEntries = true
    )
    public void supprimer(
            Long id
    ) {

        demandeRepository.deleteById(id);
    }
}