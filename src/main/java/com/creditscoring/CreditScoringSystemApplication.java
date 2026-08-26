package com.creditscoring;

import com.creditscoring.entity.Role;
import com.creditscoring.entity.Utilisateur;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class CreditScoringSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditScoringSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // ==========================
            // CREATION DES ROLES
            // ==========================

            Role adminRole = createRoleIfNotExists(
                    roleRepository,
                    "ADMIN",
                    "Administrateur du système"
            );

            createRoleIfNotExists(
                    roleRepository,
                    "CLIENT",
                    "Client de la banque"
            );

            createRoleIfNotExists(
                    roleRepository,
                    "CONSEILLER",
                    "Conseiller bancaire"
            );

            createRoleIfNotExists(
                    roleRepository,
                    "DIRECTEUR",
                    "Directeur d'agence"
            );

            createRoleIfNotExists(
                    roleRepository,
                    "RESPONSABLE_CREDIT",
                    "Responsable Crédit"
            );

            // ==========================
            // CREATION DU PREMIER ADMIN
            // ==========================

            if (utilisateurRepository
                    .findByEmail("takwabouabid149@gmail.com")
                    .isEmpty()) {

                Utilisateur admin = Utilisateur.builder()
                        .nom("Takwa")
                        .prenom("Admin")
                        .email("takwabouabid149@gmail.com")
                        .motDePasse(passwordEncoder.encode("admin123"))
                        .actif(true)
                        .role(adminRole)
                        .build();

                utilisateurRepository.save(admin);

                System.out.println("========================================");
                System.out.println("ADMIN CREE AVEC SUCCES");
                System.out.println("Email : takwabouabid149@gmail.com");
                System.out.println("Mot de passe : admin123");
                System.out.println("========================================");
            } else {

                System.out.println("L'administrateur existe déjà.");
            }

        };
    }

    private Role createRoleIfNotExists(
            RoleRepository repository,
            String nom,
            String description) {

        return repository.findByNom(nom)
                .orElseGet(() -> {

                    Role role = new Role();
                    role.setNom(nom);
                    role.setDescription(description);

                    System.out.println("Role créé : " + nom);

                    return repository.save(role);
                });
    }
}