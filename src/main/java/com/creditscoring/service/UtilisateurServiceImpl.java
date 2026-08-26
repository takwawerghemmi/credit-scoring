package com.creditscoring.service;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    @Override
    public UtilisateurResponse creerClient(BanqueRequest.RegisterClientRequest request) {

        Role role = roleRepository.findByNom("CLIENT")
                .orElseThrow(() -> new RuntimeException("Role CLIENT introuvable"));

        Client client = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .cin(request.getCin())
                .dateNaissance(request.getDateNaissance())
                .profession(request.getProfession())
                .revenuMensuel(request.getRevenuMensuel())
                .role(role)
                .build();

        clientRepository.save(client);

        return UtilisateurResponse.builder()
                .id(client.getId())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adresse(client.getAdresse())
                .actif(client.getActif())
                .role(role.getNom())
                .build();
    }

    @Override
    public UtilisateurResponse obtenirUtilisateur(Long id) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return UtilisateurResponse.builder()
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .telephone(utilisateur.getTelephone())
                .adresse(utilisateur.getAdresse())
                .actif(utilisateur.getActif())
                .role(utilisateur.getRole().getNom())
                .build();
    }

    @Override
    public List<UtilisateurResponse> obtenirTousLesUtilisateurs() {

        return utilisateurRepository.findAll()
                .stream()
                .map(u -> UtilisateurResponse.builder()
                        .id(u.getId())
                        .nom(u.getNom())
                        .prenom(u.getPrenom())
                        .email(u.getEmail())
                        .telephone(u.getTelephone())
                        .adresse(u.getAdresse())
                        .actif(u.getActif())
                        .role(u.getRole().getNom())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public UtilisateurResponse modifierUtilisateur(Long id, UpdateUtilisateurRequest request) {

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setAdresse(request.getAdresse());
        utilisateur.setActif(request.getActif());

        utilisateurRepository.save(utilisateur);

        return obtenirUtilisateur(id);
    }

    @Override
    public void supprimerUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Override
    public UtilisateurResponse obtenirParEmail(String email) {
        Utilisateur u = trouverParEmail(email);
        return UtilisateurResponse.builder()
                .id(u.getId())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .telephone(u.getTelephone())
                .adresse(u.getAdresse())
                .actif(u.getActif())
                .role(u.getRole() != null ? u.getRole().getNom() : null)
                .build();
    }

    @Override
    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

}