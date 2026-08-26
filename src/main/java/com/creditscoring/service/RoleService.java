package com.creditscoring.service;

import com.creditscoring.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    Role ajouterRole(Role role);

    List<Role> afficherTousLesRoles();

    Optional<Role> trouverRoleParId(Long id);

    Optional<Role> trouverRoleParNom(String nom);

    Role modifierRole(Long id, Role role);

    void supprimerRole(Long id);

}