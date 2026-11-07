package com.kora.backend.dto.response;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
		Instant timestamp,
		int status,
		String code,
		String error,
		String message,
		String path,
		Map<String, String> fieldErrors
) {

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
