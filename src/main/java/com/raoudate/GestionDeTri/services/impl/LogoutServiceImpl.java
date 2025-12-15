package com.raoudate.GestionDeTri.services.impl;

import com.raoudate.GestionDeTri.model.Token;
import com.raoudate.GestionDeTri.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LogoutServiceImpl implements LogoutHandler {

	private final TokenRepository tokenRepository;

	private static final String AUTH_HEADER = "Authorization";

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) {
		String authHeader = request.getHeader(AUTH_HEADER);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return;
		}
		String jwt = authHeader.substring(7);
		Optional<Token> tokenOptional = tokenRepository.findByToken(jwt);
		tokenOptional.ifPresent(tokenRepository::delete);
	}
}
