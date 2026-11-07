package com.kora.backend.dto.response;

public record SignUpResponse(
		String message,
		UserResponse user
) {
}
