
package com.creditscoring.security.config;

import com.creditscoring.security.OAuth2AuthenticationFailureHandler;
import com.creditscoring.security.OAuth2AuthenticationSuccessHandler;
import com.creditscoring.security.jwt.JwtAuthenticationFilter;
import com.creditscoring.security.oauth.GoogleOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final UserDetailsService userDetailsService;

    private final GoogleOAuth2UserService googleOAuth2UserService;

    private final OAuth2AuthenticationSuccessHandler
            oAuth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler
            oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http

                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================
                        // ROUTES PUBLIQUES
                        // =========================
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/api/auth/google-login",
                                "/api/auth/captcha",
                                "/api/auth/verify-email",
                                "/api/auth/verify-email-code",
                                "/api/auth/verify-phone",
                                "/api/auth/verify-2fa-code",
                                "/api/auth/verify-2fa-link",
                                "/api/password/**",
                                "/oauth2/**",
                                "/login/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs",
                                "/api-docs/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // =========================
                        // ADMIN
                        // =========================
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/employes/**")
                        .hasRole("ADMIN")

                        // =========================
                        // CLIENT
                        // =========================
                        .requestMatchers("/api/client/**")
                        .hasRole("CLIENT")

                        // =========================
                        // CONSEILLER
                        // =========================
                        .requestMatchers("/api/conseiller/**")
                        .hasRole("CONSEILLER")

                        // =========================
                        // RESPONSABLE CREDIT
                        // =========================
                        .requestMatchers("/api/responsable/**")
                        .hasRole("RESPONSABLE_CREDIT")



                        // =========================
                        // DEMANDES DE CRÉDIT
                        // CLIENT + CONSEILLER
                        // =========================
                        .requestMatchers("/api/demandes/**")
                        .authenticated()

                        // =========================
                        // TOUT LE RESTE
                        // AUTHENTIFICATION OBLIGATOIRE
                        // =========================
                        .anyRequest()
                        .authenticated()
                )

                .oauth2Login(oauth -> oauth

                        .userInfoEndpoint(user ->
                                user.userService(
                                        googleOAuth2UserService
                                )
                        )

                        .successHandler(
                                oAuth2AuthenticationSuccessHandler
                        )

                        .failureHandler(
                                oAuth2AuthenticationFailureHandler
                        )

                )

                .authenticationProvider(authenticationProvider())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();

    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;

    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

}