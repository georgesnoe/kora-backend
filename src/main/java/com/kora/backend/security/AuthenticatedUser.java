package com.kora.backend.security;

public record AuthenticatedUser(
		String id,
		String email,
		String sessionId
) {
}
