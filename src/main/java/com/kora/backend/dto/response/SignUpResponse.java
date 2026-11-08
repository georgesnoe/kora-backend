package com.kora.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SignUpResponse", description = "Result of account creation.")
public record SignUpResponse(
		@Schema(description = "Operation message", example = "Account created. Please check your inbox to verify your email address.", requiredMode = Schema.RequiredMode.REQUIRED) String message,

		@Schema(description = "Created user", requiredMode = Schema.RequiredMode.REQUIRED) UserResponse user) {
}
