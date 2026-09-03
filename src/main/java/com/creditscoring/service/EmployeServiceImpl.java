package com.creditscoring.service;

import com.creditscoring.dto.request.CreateEmployeRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;
import com.creditscoring.entity.*;
import com.creditscoring.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements EmployeService {

    private final AdministrateurRepository administrateurRepository;
    private final ConseillerRepository conseillerRepository;
    private final ResponsableCreditRepository responsableCreditRepository;
    private final RoleRepository roleRepository;
    private final BanqueRepository banqueRepository;
    private final AgenceRepository agenceRepository;
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // CREER ADMINISTRATEUR
    // =========================================================

    @Override
    @Transactional
    public UtilisateurResponse creerAdministrateur(
            CreateEmployeRequest request
    ) {

        Role role =
                roleRepository.findByNom("ADMIN")
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role ADMIN introuvable"
                                )
                        );

        Administrateur admin =
                Administrateur.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(
                                passwordEncoder.encode(
                                        request.getMotDePasse()
                                )
                        )
                        .telephone(request.getTelephone())
                        .adresse(request.getAdresse())
                        .matricule(request.getMatricule())
                        .departement("Administration")
                        .role(role)
                        .build();

        administrateurRepository.save(admin);

        return UtilisateurResponse.builder()
                .id(admin.getId())
                .nom(admin.getNom())
                .prenom(admin.getPrenom())
                .email(admin.getEmail())
                .telephone(admin.getTelephone())
                .adresse(admin.getAdresse())
                .actif(admin.getActif())
                .role(role.getNom())
                .build();
    }

    // =========================================================
    // CREER CONSEILLER
    // =========================================================

    @Override
    @Transactional
    public UtilisateurResponse creerConseiller(
            CreateEmployeRequest request
    ) {

        // -----------------------------------------------------
        // Vérifier role
        // -----------------------------------------------------

        Role role =
                roleRepository.findByNom("CONSEILLER")
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Role CONSEILLER introuvable"
                                )
                        );

        // -----------------------------------------------------
        // Vérifier Banque
        // -----------------------------------------------------

        if (request.getBanqueId() == null) {

            throw new RuntimeException(
                    "La banque est obligatoire pour un Conseiller."
            );
        }

        Banque banque =
                banqueRepository.findById(
                        request.getBanqueId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Banque introuvable."
                        )
                );

        // -----------------------------------------------------
        // Vérifier Agence
        // -----------------------------------------------------

        if (request.getAgenceId() == null) {

            throw new RuntimeException(
                    "L'agence est obligatoire pour un Conseiller."
            );
        }

        Agence agence =
                agenceRepository.findById(
                        request.getAgenceId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Agence introuvable."
                        )
                );

        // -----------------------------------------------------
        // Vérifier cohérence Banque / Agence
        // -----------------------------------------------------

        if (agence.getBanque() == null
                || !agence.getBanque()
                .getId()
                .equals(banque.getId())) {

            throw new RuntimeException(
                    "L'agence sélectionnée n'appartient pas à la banque sélectionnée."
            );
        }

        // -----------------------------------------------------
        // Créer Conseiller
        // -----------------------------------------------------

        Conseiller conseiller =
                Conseiller.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(
                                passwordEncoder.encode(
                                        request.getMotDePasse()
                                )
                        )
                        .telephone(request.getTelephone())
                        .adresse(request.getAdresse())
                        .matricule(request.getMatricule())
                        .specialite("Crédit")
                        .banque(banque)
                        .agence(agence)
                        .role(role)
                        .build();

        conseillerRepository.save(
                conseiller
        );

        return UtilisateurResponse.builder()
                .id(conseiller.getId())
                .nom(conseiller.getNom())
                .prenom(conseiller.getPrenom())
                .email(conseiller.getEmail())
                .telephone(conseiller.getTelephone())
                .adresse(conseiller.getAdresse())
                .actif(conseiller.getActif())
                .role(role.getNom())
                .build();
    }




    // =========================================================
    // CREER RESPONSABLE CREDIT
    // =========================================================

    @Override
    @Transactional
    public UtilisateurResponse creerResponsableCredit(
            CreateEmployeRequest request
    ) {

        // -----------------------------------------------------
        // Vérifier role
        // -----------------------------------------------------

        Role role =
                roleRepository.findByNom(
                        "RESPONSABLE_CREDIT"
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Role RESPONSABLE_CREDIT introuvable"
                        )
                );

        // -----------------------------------------------------
        // Vérifier Agence
        // -----------------------------------------------------

        if (request.getAgenceId() == null) {

            throw new RuntimeException(
                    "L'agence est obligatoire pour un Responsable Crédit."
            );
        }

        Agence agence =
                agenceRepository.findById(
                        request.getAgenceId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Agence introuvable."
                        )
                );

        // -----------------------------------------------------
        // Créer Responsable
        // -----------------------------------------------------

        ResponsableCredit responsable =
                ResponsableCredit.builder()
                        .nom(request.getNom())
                        .prenom(request.getPrenom())
                        .email(request.getEmail())
                        .motDePasse(
                                passwordEncoder.encode(
                                        request.getMotDePasse()
                                )
                        )
                        .telephone(request.getTelephone())
                        .adresse(request.getAdresse())
                        .matricule(request.getMatricule())
                        .agence(agence)
                        .role(role)
                        .build();

        responsableCreditRepository.save(
                responsable
        );

        return UtilisateurResponse.builder()
                .id(responsable.getId())
                .nom(responsable.getNom())
                .prenom(responsable.getPrenom())
                .email(responsable.getEmail())
                .telephone(responsable.getTelephone())
                .adresse(responsable.getAdresse())
                .actif(responsable.getActif())
                .role(role.getNom())
                .build();
    }
}