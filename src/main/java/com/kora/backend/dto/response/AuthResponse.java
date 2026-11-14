package com.kora.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AuthResponse", description = "Token pair and authenticated user returned by sign-in and refresh.")
public record AuthResponse(
		@Schema(description = "Signed HS256 access JWT", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED) String accessToken,

		@Schema(description = "Signed HS256 refresh JWT; a new token is issued on every refresh", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED) String refreshToken,

		@Schema(description = "Authorization scheme to use with the access token", example = "Bearer", requiredMode = Schema.RequiredMode.REQUIRED) String tokenType,

		@Schema(description = "Access token lifetime in seconds", example = "3600", requiredMode = Schema.RequiredMode.REQUIRED) long expiresIn,

		@Schema(description = "Authenticated user", requiredMode = Schema.RequiredMode.REQUIRED) UserResponse user) {
}
