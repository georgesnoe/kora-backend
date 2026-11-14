package com.kora.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(
		String from,
		String fromName) {

	public MailProperties {
		if (from == null || from.isBlank()) {
			from = "no-reply@kora.local";
		}

		if (fromName == null || fromName.isBlank()) {
			fromName = "Kora";
		}
	}

}
