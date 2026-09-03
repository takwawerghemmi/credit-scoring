package com.creditscoring.service;

import com.creditscoring.dto.request.GarantieRequest;
import com.creditscoring.dto.reponse.GarantieResponse;
import com.creditscoring.entity.DemandeCredit;
import com.creditscoring.entity.Garantie;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.mapper.GarantieMapper;
import com.creditscoring.repository.DemandeCreditRepository;
import com.creditscoring.repository.GarantieRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GarantieServiceImpl implements GarantieService {

    private final GarantieRepository garantieRepository;
    private final DemandeCreditRepository demandeRepository;
    private final UtilisateurRepository utilisateurRepository;

    // =====================================================
    // AJOUTER GARANTIE
    // =====================================================

    @Override
    public GarantieResponse ajouter(
            GarantieRequest request,
            String emailUtilisateur,
            String roleUtilisateur
    ) {

        DemandeCredit demande =
                demandeRepository.findById(
                        request.getDemandeCreditId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Demande introuvable"
                        )
                );

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable"
                                )
                        );

        verifierAccesDemande(
                demande,
                utilisateur,
                roleUtilisateur
        );

        Garantie garantie =
                Garantie.builder()
                        .type(request.getType())
                        .valeur(request.getValeur())
                        .description(request.getDescription())
                        .demandeCredit(demande)
                        .build();

        garantieRepository.save(garantie);

        return GarantieMapper.toResponse(garantie);
    }

    // =====================================================
    // AFFICHER GARANTIES
    // =====================================================

    @Override
    public List<GarantieResponse> afficherToutes(
            String emailUtilisateur,
            String roleUtilisateur
    ) {

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable"
                                )
                        );

        List<Garantie> garanties;

        switch (roleUtilisateur) {

            case "ROLE_ADMIN" ->
                    garanties = garantieRepository.findAll();

            case "ROLE_CONSEILLER" ->
                    garanties =
                            garantieRepository
                                    .findByDemandeCreditConseillerId(
                                            utilisateur.getId()
                                    );

            case "ROLE_RESPONSABLE_CREDIT" ->
                    garanties =
                            garantieRepository
                                    .findByDemandeCreditResponsableId(
                                            utilisateur.getId()
                                    );

            case "ROLE_CLIENT" ->
                    garanties =
                            garantieRepository
                                    .findByDemandeCreditClientId(
                                            utilisateur.getId()
                                    );

            default ->
                    throw new RuntimeException(
                            "Accès interdit"
                    );
        }

        return garanties
                .stream()
                .map(GarantieMapper::toResponse)
                .toList();
    }

    // =====================================================
    // AFFICHER GARANTIES D'UNE DEMANDE
    // =====================================================

    @Override
    public List<GarantieResponse> afficherParDemande(
            Long demandeId,
            String emailUtilisateur,
            String roleUtilisateur
    ) {

        DemandeCredit demande =
                demandeRepository.findById(demandeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Demande introuvable"
                                )
                        );

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable"
                                )
                        );

        verifierAccesDemande(
                demande,
                utilisateur,
                roleUtilisateur
        );

        return garantieRepository
                .findByDemandeCreditId(demandeId)
                .stream()
                .map(GarantieMapper::toResponse)
                .toList();
    }

    // =====================================================
    // SUPPRIMER
    // =====================================================

    @Override
    public void supprimer(
            Long id,
            String emailUtilisateur,
            String roleUtilisateur
    ) {

        Garantie garantie =
                garantieRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Garantie introuvable"
                                )
                        );

        DemandeCredit demande =
                garantie.getDemandeCredit();

        Utilisateur utilisateur =
                utilisateurRepository
                        .findByEmail(emailUtilisateur)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur connecté introuvable"
                                )
                        );

        verifierAccesDemande(
                demande,
                utilisateur,
                roleUtilisateur
        );

        garantieRepository.delete(garantie);
    }

    // =====================================================
    // VÉRIFICATION ACCÈS
    // =====================================================

    private void verifierAccesDemande(
            DemandeCredit demande,
            Utilisateur utilisateur,
            String roleUtilisateur
    ) {

        if ("ROLE_ADMIN".equals(roleUtilisateur)) {
            return;
        }

        if ("ROLE_CLIENT".equals(roleUtilisateur)) {

            if (demande.getClient() == null
                    || !Objects.equals(
                    demande.getClient().getId(),
                    utilisateur.getId()
            )) {

                throw new RuntimeException(
                        "Accès interdit : cette demande n'appartient pas au Client connecté."
                );
            }

            return;
        }

        if ("ROLE_CONSEILLER".equals(roleUtilisateur)) {

            if (demande.getConseiller() == null
                    || !Objects.equals(
                    demande.getConseiller().getId(),
                    utilisateur.getId()
            )) {

                throw new RuntimeException(
                        "Accès interdit : cette demande est affectée à un autre Conseiller."
                );
            }

            return;
        }

        if ("ROLE_RESPONSABLE_CREDIT".equals(roleUtilisateur)) {

            if (demande.getResponsable() == null
                    || !Objects.equals(
                    demande.getResponsable().getId(),
                    utilisateur.getId()
            )) {

                throw new RuntimeException(
                        "Accès interdit : cette demande est affectée à un autre Responsable."
                );
            }

            return;
        }

        throw new RuntimeException(
                "Accès interdit."
        );
    }
}