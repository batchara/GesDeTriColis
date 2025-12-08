package com.raoudate.GestionDeTri.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static com.raoudate.GestionDeTri.enums.Permission.*;
import static com.raoudate.GestionDeTri.enums.RoleType.ADMIN;
import static com.raoudate.GestionDeTri.enums.RoleType.SUPERVISEUR;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity

public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private final AuthenticationProvider authenticationProvider;

    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(red ->
                        red.requestMatchers(
                                        "/auth/**",
                                        "/api/v1/auth/**",
                                        "/v2/api-docs",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/webjars/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**"
                                ).permitAll()
                                
                                // Endpoint d'inscription réservé aux administrateurs uniquement
                                .requestMatchers("/auth/register").hasRole(ADMIN.name())


                                .requestMatchers("/api/v1/management/**").hasAnyRole(ADMIN.name(), SUPERVISEUR.name())

                                .requestMatchers(GET,"/api/v1/management/**").hasAnyAuthority(ADMIN_READ.name(), SUPERVISEUR_READ.name())
                                .requestMatchers(POST,"/api/v1/management/**").hasAnyAuthority(ADMIN_CREATE.name(), SUPERVISEUR_CREATE.name())
                                .requestMatchers(PUT,"/api/v1/management/**").hasAnyAuthority(ADMIN_UPDATE.name(), SUPERVISEUR_UPDATE.name())
                                .requestMatchers(DELETE,"/api/v1/management/**").hasAnyAuthority(ADMIN_DELETE.name(), SUPERVISEUR_DELETE.name())

                                .requestMatchers(GET,"/api/v1/admin/**").hasRole(ADMIN.name())

                                .requestMatchers(GET,"/api/v1/admin/**").hasAuthority(ADMIN_READ.name())
                                .requestMatchers(POST,"/api/v1/admin/**").hasAuthority(ADMIN_CREATE.name())
                                .requestMatchers(PUT,"/api/v1/admin/**").hasAuthority(ADMIN_UPDATE.name())
                                .requestMatchers(DELETE,"/api/v1/admin/**").hasAuthority(ADMIN_DELETE.name())

                                .requestMatchers("/home/**").permitAll()
                                // Permettre la lecture des agences sans authentification (pour tests)
                                .requestMatchers(GET, "/api/v1/agences/**").permitAll()
                                // Permettre l'accès à l'OCR sans authentification (pour tests)
                                .requestMatchers("/scan/**").permitAll()
                                // Les autres opérations sur les agences nécessitent une authentification
                                .requestMatchers(POST, "/api/v1/agences/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERVISEUR", "ROLE_OPERATEUR")
                                .requestMatchers(PUT, "/api/v1/agences/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERVISEUR", "ROLE_OPERATEUR")
                                .requestMatchers(DELETE, "/api/v1/agences/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERVISEUR", "ROLE_OPERATEUR")
                                
                                // Colis - Tous les utilisateurs authentifiés peuvent accéder
                                .requestMatchers("/api/v1/colis/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERVISEUR", "ROLE_OPERATEUR")
                                
                                // Audit - Réservé aux admins (ROLE_ADMIN ou permission ADMIN_READ)
                                .requestMatchers("/api/v1/audit/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN_READ")
                                
                                // Profil utilisateur - Permettre à tous les utilisateurs authentifiés d'accéder à leur propre profil
                                .requestMatchers(GET, "/api/v1/users/me").authenticated()
                                .requestMatchers(PUT, "/api/v1/users/me").authenticated()
                                // Les autres endpoints /users nécessitent ADMIN ou SUPERVISEUR
                                .requestMatchers("/api/v1/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERVISEUR")
                                
                                // OCR endpoints
                                .requestMatchers("/api/v1/ocr/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_OPERATEUR")
                                
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler(logoutHandler)
                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                )
        ;


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autoriser l'UI Angular en dev : localhost + accès via IP du réseau local
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:4200",
            "http://127.0.0.1:4200",
            "http://*:4200"  // Permet l'accès depuis n'importe quelle IP sur le port 4200
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight requests for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}


