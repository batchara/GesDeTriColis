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

import static com.raoudate.GestionDeTri.Enum.Permission.*;
import static com.raoudate.GestionDeTri.Enum.RoleType.ADMIN;
import static com.raoudate.GestionDeTri.Enum.RoleType.SUPERVISEUR;
import static org.springframework.http.HttpMethod.*;

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
            // IMPORTANT: activer CORS côté Spring Security
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(red -> red
                .requestMatchers(OPTIONS, "/**").permitAll()

                // publics
                .requestMatchers(
                    "/auth/**",
                    "/api/v1/auth/register",
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

                // management
                .requestMatchers("/api/v1/management/**").hasAnyRole(ADMIN.name(), SUPERVISEUR.name())
                .requestMatchers(GET,    "/api/v1/management/**").hasAnyAuthority(ADMIN_READ.name(),    SUPERVISEUR_READ.name())
                .requestMatchers(POST,   "/api/v1/management/**").hasAnyAuthority(ADMIN_CREATE.name(),  SUPERVISEUR_CREATE.name())
                .requestMatchers(PUT,    "/api/v1/management/**").hasAnyAuthority(ADMIN_UPDATE.name(),  SUPERVISEUR_UPDATE.name())
                .requestMatchers(DELETE, "/api/v1/management/**").hasAnyAuthority(ADMIN_DELETE.name(),  SUPERVISEUR_DELETE.name())

                // admin
                .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.name())
                .requestMatchers(GET,    "/api/v1/admin/**").hasAuthority(ADMIN_READ.name())
                .requestMatchers(POST,   "/api/v1/admin/**").hasAuthority(ADMIN_CREATE.name())
                .requestMatchers(PUT,    "/api/v1/admin/**").hasAuthority(ADMIN_UPDATE.name())
                .requestMatchers(DELETE, "/api/v1/admin/**").hasAuthority(ADMIN_DELETE.name())

                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
            );

        return http.build();
    }

    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Liste EXACTE des origines que tu utilises en dev (seulement Angular en local)
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:4200"
    ));

    // Ne PAS mélanger avec setAllowedOriginPatterns si allowCredentials=true
    // => supprimer tout appel à setAllowedOriginPatterns
    // cfg.setAllowedOriginPatterns(...); // à ne pas utiliser ici

    // Méthodes
    configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS","HEAD","PATCH"));

    // ⚠️ Assouplir les headers pour éviter la casse/minuscule/majuscule
    configuration.setAllowedHeaders(Arrays.asList("*"));

    // Headers exposés côté navigateur si tu veux les lire
    configuration.setExposedHeaders(Arrays.asList("Authorization","Location","Content-Disposition"));

    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}}
