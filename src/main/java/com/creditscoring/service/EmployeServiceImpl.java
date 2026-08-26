package com.creditscoring.service;

import com.creditscoring.dto.request.CreateEmployeRequest;
import com.creditscoring.dto.reponse.UtilisateurResponse;
import com.creditscoring.entity.*;
import com.creditscoring.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployeServiceImpl implements EmployeService {

    private final AdministrateurRepository administrateurRepository;
    private final ConseillerRepository conseillerRepository;
    private final DirecteurRepository directeurRepository;
    private final ResponsableCreditRepository responsableCreditRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;



    @Override
    public UtilisateurResponse creerAdministrateur(CreateEmployeRequest request) {

        Role role = roleRepository.findByNom("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role ADMIN introuvable"));

        Administrateur admin = Administrateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
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
    @Override
    public UtilisateurResponse creerConseiller(CreateEmployeRequest request) {

        Role role = roleRepository.findByNom("CONSEILLER")
                .orElseThrow(() -> new RuntimeException("Role CONSEILLER introuvable"));

        Conseiller conseiller = Conseiller.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .matricule(request.getMatricule())
                .specialite("Crédit")
                .role(role)
                .build();

        conseillerRepository.save(conseiller);

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
    @Override
    public UtilisateurResponse creerDirecteur(CreateEmployeRequest request) {

        Role role = roleRepository.findByNom("DIRECTEUR")
                .orElseThrow(() -> new RuntimeException("Role DIRECTEUR introuvable"));

        Directeur directeur = Directeur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .matricule(request.getMatricule())
                .role(role)
                .build();

        directeurRepository.save(directeur);

        return UtilisateurResponse.builder()
                .id(directeur.getId())
                .nom(directeur.getNom())
                .prenom(directeur.getPrenom())
                .email(directeur.getEmail())
                .telephone(directeur.getTelephone())
                .adresse(directeur.getAdresse())
                .actif(directeur.getActif())
                .role(role.getNom())
                .build();
    }
    @Override
    public UtilisateurResponse creerResponsableCredit(CreateEmployeRequest request) {

        Role role = roleRepository.findByNom("RESPONSABLE_CREDIT")
                .orElseThrow(() -> new RuntimeException("Role RESPONSABLE_CREDIT introuvable"));

        ResponsableCredit responsable = ResponsableCredit.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .adresse(request.getAdresse())
                .matricule(request.getMatricule())
                .role(role)
                .build();

        responsableCreditRepository.save(responsable);

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