package com.kora.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyAccountRequest(
		@NotBlank(message = "Email is required")
		@Email(message = "Email must be a valid email address")
		String identifier,

		@NotBlank(message = "Verification code is required")
		String code
) {
}
