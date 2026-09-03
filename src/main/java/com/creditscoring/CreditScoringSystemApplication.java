package com.creditscoring;

import com.creditscoring.entity.Administrateur;
import com.creditscoring.entity.Role;
import com.creditscoring.repository.RoleRepository;
import com.creditscoring.repository.UtilisateurRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class CreditScoringSystemApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                CreditScoringSystemApplication.class,
                args
        );
    }

    @Bean
    CommandLineRunner initDatabase(
            RoleRepository roleRepository,
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            // =====================================================
            // 1. CRÉATION DES RÔLES
            // =====================================================

            Role adminRole =
                    createRoleIfNotExists(
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
                    "RESPONSABLE_CREDIT",
                    "Responsable Crédit"
            );

            // =====================================================
            // 2. SUPER ADMIN
            // =====================================================

            String superAdminEmail =
                    "takwabouabid149@gmail.com";

            String superAdminPassword =
                    "TAkwa123";

            if (utilisateurRepository
                    .findByEmail(superAdminEmail)
                    .isEmpty()) {

                Administrateur admin =
                        Administrateur.builder()
                                .nom("Bouabid")
                                .prenom("Takwa")
                                .email(superAdminEmail)
                                .motDePasse(
                                        passwordEncoder.encode(
                                                superAdminPassword
                                        )
                                )
                                .actif(true)
                                .role(adminRole)
                                .matricule("ADM-001")
                                .departement("Administration")
                                .build();

                utilisateurRepository.save(
                        admin
                );

                System.out.println();
                System.out.println(
                        "=============================================="
                );
                System.out.println(
                        "       SUPER ADMIN CRÉÉ"
                );
                System.out.println(
                        "=============================================="
                );
                System.out.println(
                        "Email    : "
                                + superAdminEmail
                );
                System.out.println(
                        "Password : "
                                + superAdminPassword
                );
                System.out.println(
                        "Role     : ADMIN"
                );
                System.out.println(
                        "=============================================="
                );

            } else {

                System.out.println();
                System.out.println(
                        "=============================================="
                );
                System.out.println(
                        "       SUPER ADMIN DÉJÀ EXISTANT"
                );
                System.out.println(
                        "=============================================="
                );
                System.out.println(
                        "Email : "
                                + superAdminEmail
                );
                System.out.println(
                        "Role  : ADMIN"
                );
                System.out.println(
                        "=============================================="
                );
            }
        };
    }

    // =====================================================
    // CRÉER ROLE SI ABSENT
    // =====================================================

    private Role createRoleIfNotExists(
            RoleRepository repository,
            String nom,
            String description
    ) {

        return repository
                .findByNom(nom)
                .orElseGet(() -> {

                    Role role =
                            new Role();

                    role.setNom(
                            nom
                    );

                    role.setDescription(
                            description
                    );

                    System.out.println(
                            "Role créé : "
                                    + nom
                    );

                    return repository.save(
                            role
                    );
                });
    }
}