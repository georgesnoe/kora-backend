package com.kora.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@Schema(name = "ErrorResponse", description = "Standard error envelope returned by every API error path.")
public record ErrorResponse(
		@Schema(description = "Error timestamp in UTC", example = "2026-11-07T21:52:46.178536400Z", requiredMode = Schema.RequiredMode.REQUIRED) Instant timestamp,

		@Schema(description = "HTTP status code", example = "401", requiredMode = Schema.RequiredMode.REQUIRED) int status,

		@Schema(description = "Machine-readable error code", example = "UNAUTHORIZED", requiredMode = Schema.RequiredMode.REQUIRED) String code,

		@Schema(description = "HTTP reason phrase", example = "Unauthorized", requiredMode = Schema.RequiredMode.REQUIRED) String error,

		@Schema(description = "Human-readable error message", example = "Authentication is required to access this resource", requiredMode = Schema.RequiredMode.REQUIRED) String message,

		@Schema(description = "Request path that produced the error", example = "/api/auth/refresh", requiredMode = Schema.RequiredMode.REQUIRED) String path,

		@Schema(description = "Field name to validation message map; empty when the error is not a "
				+ "field-validation error", example = "{\"email\":\"Email must be a valid email address\"}") Map<String, String> fieldErrors) {

	public static ErrorResponse of(HttpStatus status, String code, String message, String path) {
		return of(status, code, message, path, Map.of());
	}

	public static ErrorResponse of(
			HttpStatus status,
			String code,
			String message,
			String path,
			Map<String, String> fieldErrors) {

		return new ErrorResponse(
				Instant.now(),
				status.value(),
				code,
				status.getReasonPhrase(),
				message,
				path,
				fieldErrors);
	}

}
