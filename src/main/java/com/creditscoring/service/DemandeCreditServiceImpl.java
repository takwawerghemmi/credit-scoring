package com.creditscoring.service;

import com.creditscoring.entity.Conseiller;
import com.creditscoring.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import com.creditscoring.dto.request.DemandeCreditRequest;
import com.creditscoring.dto.reponse.DemandeCreditResponse;
import com.creditscoring.entity.Banque;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.enums.StatutDemande;
import com.creditscoring.mapper.DemandeCreditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandeCreditServiceImpl
        implements DemandeCreditService {

    private final DemandeCreditRepository demandeRepository;
    private final ClientRepository clientRepository;
    private final BanqueRepository banqueRepository;
    private final NotificationService notificationService;
    private final ConseillerRepository conseillerRepository;
    private final ResponsableCreditRepository responsableCreditRepository;
    // =========================================================
    // CRÉER UNE DEMANDE
    // CLIENT
    // =========================================================

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

        // =====================================================
        // Récupérer le client connecté
        // =====================================================

        Client client =
                clientRepository.findByEmail(
                        emailUtilisateur
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Client connecté introuvable"
                        )
                );

        // =====================================================
        // Récupérer la banque
        // =====================================================

        Banque banque =
                banqueRepository.findById(
                        request.getBanqueId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Banque introuvable"
                        )
                );

        // =====================================================
        // Créer la demande
        //
        // IMPORTANT:
        // Le Client ne choisit ni Conseiller
        // ni Responsable.
        //
        // L'affectation sera faite ensuite par l'ADMIN.
        // =====================================================

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
                        .banque(banque)
                        .build();

        demandeRepository.save(
                demande
        );

        // =====================================================
        // Pas de notification Conseiller ici.
        //
        // La notification sera envoyée par l'ADMIN
        // lorsqu'il affectera la demande.
        // =====================================================

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    // =========================================================
    // MODIFIER UNE DEMANDE
    // =========================================================

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

        // =====================================================
        // Banque
        // =====================================================

        Banque banque =
                banqueRepository.findById(
                        request.getBanqueId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Banque introuvable"
                        )
                );

        // =====================================================
        // Mise à jour
        // =====================================================

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

        demande.setClient(
                client
        );

        demande.setBanque(
                banque
        );

        demandeRepository.save(
                demande
        );

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    // =========================================================
    // MODIFIER POUR UN UTILISATEUR
    // =========================================================

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

        // =====================================================
        // CLIENT
        // =====================================================

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

    // =========================================================
    // TROUVER PAR ID
    // =========================================================

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

    // =========================================================
    // TROUVER PAR ID POUR UN UTILISATEUR
    // =========================================================

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

        // =====================================================
        // CONSEILLER
        //
        // Il ne doit voir que ses demandes affectées.
        // =====================================================

        if ("ROLE_CONSEILLER".equals(role)) {

            if (demande.getConseiller() == null
                    || demande.getConseiller()
                    .getEmail() == null
                    || !demande.getConseiller()
                    .getEmail()
                    .equalsIgnoreCase(
                            emailUtilisateur
                    )) {

                throw new RuntimeException(
                        "Accès interdit : cette demande n'est pas affectée à ce Conseiller."
                );
            }

            return DemandeCreditMapper.toResponse(
                    demande
            );
        }

        // =====================================================
        // RESPONSABLE CRÉDIT
        //
        // Il ne doit voir que ses demandes affectées.
        // =====================================================

        if ("ROLE_RESPONSABLE_CREDIT".equals(role)) {

            if (demande.getResponsable() == null
                    || demande.getResponsable()
                    .getEmail() == null
                    || !demande.getResponsable()
                    .getEmail()
                    .equalsIgnoreCase(
                            emailUtilisateur
                    )) {

                throw new RuntimeException(
                        "Accès interdit : cette demande n'est pas affectée à ce Responsable."
                );
            }

            return DemandeCreditMapper.toResponse(
                    demande
            );
        }

        // =====================================================
        // CLIENT
        // =====================================================

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

        return DemandeCreditMapper.toResponse(
                demande
        );
    }

    // =========================================================
    // AFFICHER TOUTES LES DEMANDES
    // =========================================================

    @Override
    public List<DemandeCreditResponse> afficherToutes() {

        return demandeRepository.findAll()
                .stream()
                .map(
                        DemandeCreditMapper::toResponse
                )
                .toList();
    }

    // =========================================================
    // MES DEMANDES - CLIENT
    // =========================================================

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

    // =========================================================
    // DEMANDES PAR CLIENT
    // =========================================================

    @Override
    public List<DemandeCreditResponse> afficherParClient(
            Long clientId
    ) {

        return demandeRepository
                .findByClientId(
                        clientId
                )
                .stream()
                .map(
                        DemandeCreditMapper::toResponse
                )
                .toList();
    }

    // =========================================================
    // SUPPRIMER POUR UN UTILISATEUR
    // =========================================================

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
                demandeRepository.findById(
                        id
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        // =====================================================
        // CLIENT
        // =====================================================

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

    // =========================================================
    // SUPPRIMER
    // =========================================================

    @Override
    @Transactional
    @CacheEvict(
            value = "dashboard",
            allEntries = true
    )
    public void supprimer(
            Long id
    ) {

        demandeRepository.deleteById(
                id
        );
    }
    @Override
    public List<DemandeCreditResponse> afficherParConseiller(
            String emailUtilisateur
    ) {

        Conseiller conseiller =
                conseillerRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Conseiller connecté introuvable"
                                )
                        );

        return demandeRepository
                .findByConseillerId(conseiller.getId())
                .stream()
                .map(DemandeCreditMapper::toResponse)
                .toList();
    }
}