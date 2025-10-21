package com.raoudate.GestionDeTri.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import com.raoudate.GestionDeTri.repository.TokenRepository;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Ne pas traiter les requêtes preflight CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if(request.getServletPath().contains("/api/v1/auth") ) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        // sanitize token string
        jwt = authHeader.substring(7).trim().replace("\"", "").replace("'", "");
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (io.jsonwebtoken.JwtException ex) {
            // malformed/invalid token -> log and continue filter chain without authentication
            LOGGER.debug("Invalid JWT token: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }
        // check token exists in DB (not revoked)
        if (!tokenRepository.findByToken(jwt).isPresent()) {
            filterChain.doFilter(request, response);
            return;
        }
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if(jwtService.validateToken(jwt, userDetails)){
                // try to extract authorities from token claims if present
                try {
                    Claims claims = jwtService.extractAllClaims(jwt);
                    @SuppressWarnings("unchecked")
                    java.util.List<String> roles = (java.util.List<String>) claims.get("authorities", java.util.List.class);
                    java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
                    if (roles != null) {
                        for (String r : roles) {
                            authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(r));
                        }
                    } else {
                        authorities = new java.util.ArrayList<>(userDetails.getAuthorities());
                    }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                authorities);
            // log extracted authorities for debugging
            LOGGER.debug("Authorities extracted from token for user {}: {}", userEmail, authorities);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } catch (Exception ex) {
            LOGGER.debug("Failed to extract authorities from token: {}", ex.getMessage());
                    // fallback to authorities from UserDetails
            java.util.List<org.springframework.security.core.GrantedAuthority> fallbackAuths = new java.util.ArrayList<>(userDetails.getAuthorities());
            LOGGER.debug("Fallback authorities from UserDetails for user {}: {}", userEmail, fallbackAuths);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                fallbackAuths);
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        }
        filterChain.doFilter(request, response);
    }
}
