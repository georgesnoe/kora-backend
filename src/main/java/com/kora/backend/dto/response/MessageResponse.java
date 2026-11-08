package com.kora.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MessageResponse", description = "Simple human-readable operation result.")
public record MessageResponse(
		@Schema(description = "Result message", example = "Signed out successfully.", requiredMode = Schema.RequiredMode.REQUIRED) String message) {
}
