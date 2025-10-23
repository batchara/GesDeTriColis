package com.raoudate.GestionDeTri.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
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

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println("🔐 [JwtAuthFilter] Requête: " + request.getMethod() + " " + request.getRequestURI());
        
        if(request.getServletPath().contains("/api/v1/auth") ) {
            System.out.println("✅ [JwtAuthFilter] Endpoint d'auth, skip filter");
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        System.out.println("🔍 [JwtAuthFilter] Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(30, authHeader.length())) + "..." : "null"));
        
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ [JwtAuthFilter] Pas de token Bearer");
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        userEmail = jwtService.extractUsername(jwt);
        
        System.out.println("🔍 [JwtAuthFilter] User email extrait: " + userEmail);
        
        // TEMPORAIRE : Désactiver la vérification du token en DB pour tester
        /*
        // check token exists in DB (not revoked)
        if (!tokenRepository.findByToken(jwt).isPresent()) {
            System.out.println("❌ [JwtAuthFilter] Token non trouvé en DB ou révoqué");
            filterChain.doFilter(request, response);
            return;
        }
        
        System.out.println("✅ [JwtAuthFilter] Token trouvé en DB");
        */
        
        if(userEmail != null && SecurityContextHolder.getContext().getAuthentication()==null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            
            System.out.println("🔍 [JwtAuthFilter] Authorities de l'utilisateur: " + userDetails.getAuthorities());

            if(jwtService.validateToken(jwt, userDetails)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ [JwtAuthFilter] Authentification réussie");
            } else {
                System.out.println("❌ [JwtAuthFilter] Token invalide");
            }

        }
        filterChain.doFilter(request, response);
    }
}
