package com.kora.backend.service;

import com.kora.backend.config.JwtProperties;
import com.kora.backend.dto.request.SignInRequest;
import com.kora.backend.dto.request.SignUpRequest;
import com.kora.backend.dto.response.AuthResponse;
import com.kora.backend.dto.response.MessageResponse;
import com.kora.backend.dto.response.SignUpResponse;
import com.kora.backend.dto.response.UserResponse;
import com.kora.backend.entity.Account;
import com.kora.backend.entity.Session;
import com.kora.backend.entity.User;
import com.kora.backend.exception.EmailAlreadyExistsException;
import com.kora.backend.exception.EmailSendingException;
import com.kora.backend.exception.InvalidCredentialsException;
import com.kora.backend.exception.InvalidTokenException;
import com.kora.backend.exception.AccountNotVerifiedException;
import com.kora.backend.exception.UnauthorizedException;
import com.kora.backend.repository.AccountRepository;
import com.kora.backend.repository.SessionRepository;
import com.kora.backend.repository.UserRepository;
import com.kora.backend.security.AuthenticatedUser;
import com.kora.backend.util.HashingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String CREDENTIALS_PROVIDER = "credential";
	private static final String TOKEN_TYPE = "Bearer";

	private final UserRepository userRepository;
	private final AccountRepository accountRepository;
	private final SessionRepository sessionRepository;
	private final VerificationService verificationService;
	private final EmailService emailService;
	private final JwtService jwtService;
	private final PasswordEncoder passwordEncoder;
	private final JwtProperties jwtProperties;

	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException(email);
		}

		User user = userRepository.save(User.builder()
				.name(request.name().trim())
				.email(email)
				.emailVerified(false)
				.build());

		Account account = Account.builder()
				.user(user)
				.providerId(CREDENTIALS_PROVIDER)
				.accountId(user.getId())
				.password(passwordEncoder.encode(request.password()))
				.build();
		accountRepository.save(account);

		UserResponse userResponse = UserResponse.from(user);
		sendVerificationEmail(user);

		return new SignUpResponse(
				"Account created. Please check your inbox to verify your email address.",
				userResponse);
	}

	@Transactional
	public AuthResponse signIn(SignInRequest request, String ipAddress, String userAgent) {
		String email = normalizeEmail(request.email());

		User user = userRepository.findByEmail(email)
				.orElseThrow(InvalidCredentialsException::new);

		Account account = accountRepository
				.findByUserIdAndProviderId(user.getId(), CREDENTIALS_PROVIDER)
				.orElseThrow(InvalidCredentialsException::new);

		if (account.getPassword() == null
				|| !passwordEncoder.matches(request.password(), account.getPassword())) {
			throw new InvalidCredentialsException();
		}

		if (!user.isEmailVerified()) {
			throw new AccountNotVerifiedException();
		}

		IssuedSession issuedSession = createSession(user, ipAddress, userAgent);
		return toAuthResponse(user, issuedSession);
	}

	@Transactional
	public AuthResponse refresh(String refreshToken) {
		Jwt jwt = jwtService.parseRefreshToken(refreshToken);

		Session session = sessionRepository.findByToken(HashingUtils.sha256Hex(refreshToken))
				.orElseThrow(() -> new InvalidTokenException(
						"Refresh token is invalid or has been revoked"));

		if (session.getExpiresAt().isBefore(Instant.now())) {
			sessionRepository.delete(session);
			throw new InvalidTokenException("Refresh token has expired");
		}

		User user = session.getUser();
		String sessionId = jwt.getClaimAsString("sid");

		if (!user.getId().equals(jwt.getSubject()) || !session.getId().equals(sessionId)) {
			throw new InvalidTokenException("Refresh token does not belong to this session");
		}

		JwtService.IssuedToken newRefreshToken = jwtService.generateRefreshToken(user, session.getId());
		session.setToken(HashingUtils.sha256Hex(newRefreshToken.value()));
		session.setExpiresAt(newRefreshToken.expiresAt());
		sessionRepository.save(session);

		JwtService.IssuedToken accessToken = jwtService.generateAccessToken(user, session.getId());

		return new AuthResponse(
				accessToken.value(),
				newRefreshToken.value(),
				TOKEN_TYPE,
				jwtProperties.accessTokenTtl().toSeconds(),
				UserResponse.from(user));
	}

	@Transactional
	public MessageResponse signOut(AuthenticatedUser principal, String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			sessionRepository.findByToken(HashingUtils.sha256Hex(refreshToken))
					.ifPresent(sessionRepository::delete);

			return new MessageResponse("Signed out successfully.");
		}

		if (principal == null) {
			throw new UnauthorizedException(
					"A valid access token or refresh token is required to sign out");
		}

		sessionRepository.findById(principal.sessionId())
				.ifPresent(sessionRepository::delete);

		return new MessageResponse("Signed out successfully.");
	}

	@Transactional
	public MessageResponse verify(String identifier, String code) {
		verificationService.verify(identifier, code);
		return new MessageResponse(
				"Your email address has been verified. You can now sign in.");
	}

	@Transactional
	public MessageResponse resendVerification(String email) {
		String normalizedEmail = normalizeEmail(email);

		userRepository.findByEmail(normalizedEmail)
				.filter(user -> !user.isEmailVerified())
				.ifPresent(this::sendVerificationEmail);

		return new MessageResponse(
				"If an account with that email exists and is not verified, "
						+ "a new verification email has been sent.");
	}

	private IssuedSession createSession(User user, String ipAddress, String userAgent) {
		String sessionId = UUID.randomUUID().toString();
		JwtService.IssuedToken refreshToken = jwtService.generateRefreshToken(user, sessionId);

		Session session = Session.builder()
				.token(HashingUtils.sha256Hex(refreshToken.value()))
				.expiresAt(refreshToken.expiresAt())
				.ipAddress(ipAddress)
				.userAgent(userAgent)
				.user(user)
				.build();
		session.setId(sessionId);

		sessionRepository.save(session);
		return new IssuedSession(session, refreshToken.value());
	}

	private AuthResponse toAuthResponse(User user, IssuedSession issuedSession) {
		JwtService.IssuedToken accessToken = jwtService.generateAccessToken(
				user, issuedSession.session().getId());

		return new AuthResponse(
				accessToken.value(),
				issuedSession.refreshToken(),
				TOKEN_TYPE,
				jwtProperties.accessTokenTtl().toSeconds(),
				UserResponse.from(user));
	}

	private void sendVerificationEmail(User user) {
		String code = verificationService.createVerification(user);

		try {
			emailService.sendVerificationEmail(user, code);
		} catch (EmailSendingException exception) {
			log.error("Could not send verification email to {}", user.getEmail(), exception);
		}
	}

	private String normalizeEmail(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}

	private record IssuedSession(Session session, String refreshToken) {
	}

}
