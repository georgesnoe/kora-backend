package com.kora.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "RefreshTokenRequest", description = "Refresh token issued by sign-in or by a previous refresh operation.")
public record RefreshTokenRequest(
		@Schema(description = "Signed refresh JWT", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Refresh token is required") String refreshToken) {
}
