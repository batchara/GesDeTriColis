package com.raoudate.GestionDeTri.services;

public interface LogoutService {

	/**
	 * Invalidate a refresh or access token.
	 * @param token the token to invalidate
	 */
	void logoutByToken(String token);

	/**
	 * Invalidate all tokens for a given user id.
	 * @param userId id of the user whose tokens will be invalidated
	 */
	void logoutByUserId(Integer userId);
}
