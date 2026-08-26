package com.creditscoring.service.impl;

import com.creditscoring.entity.Role;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role ajouterRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public List<Role> afficherTousLesRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Optional<Role> trouverRoleParId(Long id) {
        return roleRepository.findById(id);
    }

    @Override
    public Optional<Role> trouverRoleParNom(String nom) {
        return roleRepository.findByNom(nom);
    }

    @Override
    public Role modifierRole(Long id, Role role) {

        Role ancien = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role introuvable"));

        ancien.setNom(role.getNom());
        ancien.setDescription(role.getDescription());

        return roleRepository.save(ancien);
    }

    @Override
    public void supprimerRole(Long id) {
        roleRepository.deleteById(id);
    }

}