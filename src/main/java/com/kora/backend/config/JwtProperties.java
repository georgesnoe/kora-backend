package com.kora.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
		String secret,
		String issuer,
		Duration accessTokenTtl,
		Duration refreshTokenTtl) {

	public JwtProperties {
		if (issuer == null || issuer.isBlank()) {
			issuer = "kora";
		}

		if (accessTokenTtl == null) {
			accessTokenTtl = Duration.ofHours(1);
		}

		if (refreshTokenTtl == null) {
			refreshTokenTtl = Duration.ofDays(7);
		}
	}

}
