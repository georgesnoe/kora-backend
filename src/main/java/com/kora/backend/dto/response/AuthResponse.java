package com.kora.backend.dto.response;

public record AuthResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresIn,
		UserResponse user
) {
}
