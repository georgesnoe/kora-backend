package com.kora.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "SignInRequest", description = "Credential sign-in payload.")
public record SignInRequest(
		@Schema(description = "Account email address", example = "ada@example.com", format = "email", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") String email,

		@Schema(description = "Account password", example = "S3cretPass", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Password is required") String password) {
}
