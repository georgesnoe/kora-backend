package com.kora.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the application and writes the generated OpenAPI description to
 * {@code target/openapi.yaml}, which is the build output consumed by CI and
 * API consumers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocumentationTest {

	@Value("${local.server.port}")
	private int port;

	@Test
	void writesOpenApiSpecificationToBuildOutput() throws Exception {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + "/v3/api-docs.yaml"))
				.timeout(Duration.ofSeconds(20))
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(200);

		String yaml = response.body();

		assertThat(yaml).isNotBlank();
		assertThat(yaml).contains("openapi:");
		assertThat(yaml).contains("/api/auth/sign-up");
		assertThat(yaml).contains("/api/auth/sign-in");
		assertThat(yaml).contains("/api/auth/refresh");
		assertThat(yaml).contains("/api/auth/sign-out");
		assertThat(yaml).contains("/api/auth/verify");
		assertThat(yaml).contains("/api/auth/resend-verification");
		assertThat(yaml).contains("bearerAuth");
		assertThat(yaml).contains("SignUpRequest");

		Path output = Path.of("target", "openapi.yaml");
		Files.createDirectories(output.getParent());
		Files.writeString(output, yaml);
	}

}
