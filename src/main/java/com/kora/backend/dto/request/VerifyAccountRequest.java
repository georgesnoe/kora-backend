package com.kora.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "VerifyAccountRequest", description = "Email verification payload sent by a client after reading the verification email.")
public record VerifyAccountRequest(
		@Schema(description = "Email address being verified", example = "ada@example.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String identifier,

		@Schema(description = "Numeric verification code from the email", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Verification code is required") String code) {
}
