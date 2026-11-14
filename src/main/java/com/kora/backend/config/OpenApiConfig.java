package com.kora.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Kora Backend API", version = "0.0.1-SNAPSHOT", description = "Identity and access management API for Kora. It covers registration, "
		+ "email verification, credential sign-in, rotating JWT sessions, roles, permissions, "
		+ "enterprises, and stores. Access tokens must be sent as "
		+ "`Authorization: Bearer <accessToken>`.", contact = @Contact(name = "Kossi Georges-Noé AHOMBO", email = "georges.ahombo@gmail.com", url = "https://georgesnoe.github.io")), tags = {
				@Tag(name = "Authentication", description = "Registration, email verification, sign-in, session refresh, and sign-out.")
		}, servers = {
				@Server(url = "/", description = "Current server")
		})
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Provide a valid access token. Refresh tokens are not accepted by this scheme.")
public class OpenApiConfig {
}
