package com.kora.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.verification")
public record VerificationProperties(
		Integer codeLength,
		Duration ttl,
		String verifyUrl
) {

	public VerificationProperties {
		if (codeLength == null || codeLength < 4) {
			codeLength = 6;
		}

		if (ttl == null) {
			ttl = Duration.ofHours(24);
		}

		if (verifyUrl == null || verifyUrl.isBlank()) {
			verifyUrl = "http://localhost:8080/api/auth/verify";
		}
	}

}
