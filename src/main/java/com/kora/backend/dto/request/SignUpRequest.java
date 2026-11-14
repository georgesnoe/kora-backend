package com.kora.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "SignUpRequest", description = "Registration payload for a new credential account.")
public record SignUpRequest(
		@Schema(description = "Display name; surrounding whitespace is trimmed", example = "Ada Lovelace", minLength = 2, maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters") String name,

		@Schema(description = "Unique email address; stored in lowercase", example = "ada@example.com", format = "email", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Email is required") @Email(message = "Email must be a valid email address") @Size(max = 255, message = "Email must be at most 255 characters") String email,

		@Schema(description = "Password with at least one letter and one digit", example = "S3cretPass", minLength = 8, maxLength = 72, format = "password", accessMode = Schema.AccessMode.WRITE_ONLY, requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Password is required") @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters") @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "Password must contain at least one letter and one digit") String password) {
}
