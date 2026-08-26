package com.creditscoring.security;

import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Utilisateur introuvable"));

        return User.builder()
                .username(utilisateur.getEmail())
                .password(utilisateur.getMotDePasse())
                .roles(utilisateur.getRole().getNom())
                .build();
    }
}