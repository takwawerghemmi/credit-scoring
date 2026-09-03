package com.creditscoring.service;

import com.creditscoring.dto.request.BanqueRequest;
import com.creditscoring.dto.request.UpdateUtilisateurRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;
import com.creditscoring.entity.Client;
import com.creditscoring.entity.Role;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.ClientRepository;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;

    private final ClientRepository clientRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    // =========================================================
    // CREATION CLIENT
    // =========================================================

    @Override
    public UtilisateurResponse creerClient(
            BanqueRequest.RegisterClientRequest request) {

        Role role = roleRepository.findByNom("CLIENT")
                .orElseThrow(() ->
                        new RuntimeException(
                                "Role CLIENT introuvable"
                        )
                );

        Client client = Client.builder()
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
                .cin(request.getCin())
                .dateNaissance(request.getDateNaissance())
                .profession(request.getProfession())
                .revenuMensuel(request.getRevenuMensuel())
                .role(role)
                .build();

        clientRepository.save(client);

        return convertirEnResponse(client);
    }


    // =========================================================
    // OBTENIR UTILISATEUR PAR ID
    // =========================================================

    @Override
    public UtilisateurResponse obtenirUtilisateur(Long id) {

        Utilisateur utilisateur =
                utilisateurRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        return convertirEnResponse(utilisateur);
    }


    // =========================================================
    // OBTENIR TOUS LES UTILISATEURS
    // =========================================================

    @Override
    public List<UtilisateurResponse> obtenirTousLesUtilisateurs() {

        return utilisateurRepository.findAll()
                .stream()
                .map(this::convertirEnResponse)
                .collect(Collectors.toList());
    }


    // =========================================================
    // MODIFICATION UTILISATEUR
    // =========================================================

    @Override
    public UtilisateurResponse modifierUtilisateur(
            Long id,
            UpdateUtilisateurRequest request) {

        Utilisateur utilisateur =
                utilisateurRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Utilisateur introuvable"
                                )
                        );

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setAdresse(request.getAdresse());
        utilisateur.setActif(request.getActif());


        // =====================================================
        // CAS CLIENT
        // =====================================================

        if (utilisateur instanceof Client client) {

            if (request.getCin() != null) {
                client.setCin(request.getCin());
            }

            client.setDateNaissance(
                    request.getDateNaissance()
            );

            client.setProfession(
                    request.getProfession()
            );

            client.setTypeContratTravail(
                    request.getTypeContratTravail()
            );

            client.setSituationFamiliale(
                    request.getSituationFamiliale()
            );

            if (request.getDettesExistantes() != null) {

                client.setDettesExistantes(
                        request.getDettesExistantes()
                );
            }

            if (request.getRevenuMensuel() != null) {

                client.setRevenuMensuel(
                        request.getRevenuMensuel()
                );
            }

            if (request.getAncienneteEmploi() != null) {

                client.setAncienneteEmploi(
                        request.getAncienneteEmploi()
                );
            }

            if (request.getNombrePersonnesACharge() != null) {

                client.setNombrePersonnesACharge(
                        request.getNombrePersonnesACharge()
                );
            }

            clientRepository.save(client);

        } else {

            utilisateurRepository.save(utilisateur);
        }

        return obtenirUtilisateur(id);
    }


    // =========================================================
    // SUPPRESSION
    // =========================================================

    @Override
    public void supprimerUtilisateur(Long id) {

        utilisateurRepository.deleteById(id);
    }


    // =========================================================
    // OBTENIR PAR EMAIL
    // =========================================================

    @Override
    public UtilisateurResponse obtenirParEmail(String email) {

        Utilisateur utilisateur =
                trouverParEmail(email);

        return convertirEnResponse(utilisateur);
    }


    // =========================================================
    // CHERCHER PAR EMAIL
    // =========================================================

    @Override
    public Utilisateur trouverParEmail(String email) {

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Utilisateur introuvable"
                        )
                );
    }


    // =========================================================
    // CONVERSION ENTITY -> RESPONSE
    // =========================================================

    private UtilisateurResponse convertirEnResponse(
            Utilisateur utilisateur) {

        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .adresse(utilisateur.getAdresse())
                .actif(utilisateur.getActif())
                .role(
                        utilisateur.getRole() != null
                                ? utilisateur.getRole().getNom()
                                : null
                )
                .build();
    }
}