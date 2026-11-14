package com.kora.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "ResendVerificationRequest", description = "Email address for which a new verification code should be generated.")
public record ResendVerificationRequest(
		@Schema(description = "Email address of the unverified account", example = "ada@example.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email) {
}
