package com.kora.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SignOutRequest", description = "Optional body for sign-out. When a refresh token is supplied, its session is "
		+ "revoked; otherwise the session bound to the access token is revoked.")
public record SignOutRequest(
		@Schema(description = "Refresh token of the session to revoke", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.NOT_REQUIRED) String refreshToken) {
}
