package com.kora.backend.service;

import com.kora.backend.config.JwtProperties;
import com.kora.backend.entity.User;
import com.kora.backend.exception.InvalidTokenException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtService {

	private static final String TOKEN_TYPE_CLAIM = "token_type";
	private static final String SESSION_ID_CLAIM = "sid";
	private static final String EMAIL_CLAIM = "email";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";
	private static final int MINIMUM_SECRET_LENGTH = 32;

	private final JwtProperties properties;
	private final JwtEncoder encoder;
	private final JwtDecoder decoder;

	public JwtService(JwtProperties properties) {
		this.properties = properties;

		byte[] secret = properties.secret() == null
				? new byte[0]
				: properties.secret().getBytes(StandardCharsets.UTF_8);

		if (secret.length < MINIMUM_SECRET_LENGTH) {
			throw new IllegalStateException(
					"app.jwt.secret must be at least " + MINIMUM_SECRET_LENGTH + " bytes long");
		}

		SecretKey secretKey = new SecretKeySpec(secret, "HmacSHA256");
		this.encoder = NimbusJwtEncoder.withSecretKey(secretKey)
				.algorithm(MacAlgorithm.HS256)
				.build();

		NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
		this.decoder = jwtDecoder;
	}

	public IssuedToken generateAccessToken(User user, String sessionId) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(properties.accessTokenTtl());
		return issueToken(user, sessionId, ACCESS_TOKEN_TYPE, issuedAt, expiresAt);
	}

	public IssuedToken generateRefreshToken(User user, String sessionId) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(properties.refreshTokenTtl());
		return issueToken(user, sessionId, REFRESH_TOKEN_TYPE, issuedAt, expiresAt);
	}

	public Jwt parseAccessToken(String token) {
		return parseToken(token, ACCESS_TOKEN_TYPE);
	}

	public Jwt parseRefreshToken(String token) {
		return parseToken(token, REFRESH_TOKEN_TYPE);
	}

	private IssuedToken issueToken(User user, String sessionId, String tokenType, Instant issuedAt, Instant expiresAt) {
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.subject(user.getId())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.id(UUID.randomUUID().toString())
				.claim(TOKEN_TYPE_CLAIM, tokenType)
				.claim(SESSION_ID_CLAIM, sessionId)
				.claim(EMAIL_CLAIM, user.getEmail())
				.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(token, expiresAt);
	}

	private Jwt parseToken(String token, String expectedTokenType) {
		if (token == null || token.isBlank()) {
			throw new InvalidTokenException("Token is missing");
		}

		try {
			Jwt jwt = decoder.decode(token);
			String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);

			if (!expectedTokenType.equals(tokenType)) {
				throw new InvalidTokenException("Invalid token type");
			}

			return jwt;
		}
		catch (JwtException | IllegalArgumentException exception) {
			throw new InvalidTokenException("Invalid or expired token");
		}
	}

	public record IssuedToken(String value, Instant expiresAt) {
	}

}
