package com.kora.backend.controller;

import com.kora.backend.dto.request.RefreshTokenRequest;
import com.kora.backend.dto.request.ResendVerificationRequest;
import com.kora.backend.dto.request.SignInRequest;
import com.kora.backend.dto.request.SignOutRequest;
import com.kora.backend.dto.request.SignUpRequest;
import com.kora.backend.dto.request.VerifyAccountRequest;
import com.kora.backend.dto.response.AuthResponse;
import com.kora.backend.dto.response.ErrorResponse;
import com.kora.backend.dto.response.MessageResponse;
import com.kora.backend.dto.response.SignUpResponse;
import com.kora.backend.dto.response.UserResponse;
import com.kora.backend.security.AuthenticatedUser;
import com.kora.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Registration, email verification, sign-in, session refresh, and sign-out.")
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "Create an account", description = "Creates an unverified credential account, hashes the password with BCrypt, "
			+ "and sends a numeric verification code by email.", operationId = "signUp", tags = "Authentication")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Account created and verification email dispatched", content = @Content(schema = @Schema(implementation = SignUpResponse.class))),
			@ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "An account with this email already exists", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/sign-up")
	public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
	}

	@Operation(summary = "Sign in", description = "Authenticates a verified credential account and creates a server-side session. "
			+ "Returns a signed access token and a rotating refresh token. The client IP and user agent "
			+ "are recorded on the session.", operationId = "signIn", tags = "Authentication")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Authenticated; tokens issued", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Invalid email or password", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "Email address has not been verified", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/sign-in")
	public AuthResponse signIn(
			@Valid @RequestBody SignInRequest request,
			@Parameter(description = "Originating client IP supplied by a trusted reverse proxy. "
					+ "The first address in the list is stored on the session.", example = "203.0.113.10") @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
			@Parameter(description = "Client user agent stored on the session", example = "Mozilla/5.0") @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
			HttpServletRequest httpRequest) {

		return authService.signIn(
				request,
				clientIpAddress(forwardedFor, httpRequest),
				userAgent);
	}

	@Operation(summary = "Get the current user", description = "Returns the profile of the caller identified by the access token. The response "
			+ "never includes credentials or password hashes, and the caller's email-verification "
			+ "status is reported as-is.", operationId = "getCurrentUser", tags = "Authentication", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Authenticated user profile", content = @Content(schema = @Schema(implementation = UserResponse.class))),
			@ApiResponse(responseCode = "401", description = "Missing or invalid access token", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/me")
	public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
		return authService.me(principal);
	}

	@Operation(summary = "Refresh a session", description = "Rotates the refresh token and issues a new access token. The caller must be "
			+ "authenticated with a valid access token; refresh tokens cannot be used once the access "
			+ "token has expired, in which case the user must sign in again.", operationId = "refreshSession", tags = "Authentication", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Session refreshed; the previous refresh token is now invalid", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
			@ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Missing/invalid access token, or invalid/revoked/expired refresh token", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request.refreshToken());
	}

	@Operation(summary = "Sign out", description = "Revokes the current session, or the session identified by the supplied refresh "
			+ "token. Requires a valid access token. The operation is idempotent.", operationId = "signOut", tags = "Authentication", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Session revoked", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
			@ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/sign-out")
	public MessageResponse signOut(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Optional refresh token. When omitted, the session bound to the access "
					+ "token is revoked.", required = false, content = @Content(schema = @Schema(implementation = SignOutRequest.class))) @RequestBody(required = false) SignOutRequest request) {

		return authService.signOut(principal, request == null ? null : request.refreshToken());
	}

	@Operation(summary = "Verify an email address (JSON)", description = "Validates the numeric code sent by email and marks the account as verified. "
			+ "The code is single-use and expires after the configured TTL.", operationId = "verifyAccount", tags = "Authentication")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Email address verified", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Validation failed, or the code is invalid/expired", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/verify")
	public MessageResponse verify(@Valid @RequestBody VerifyAccountRequest request) {
		return authService.verify(request.identifier(), request.code());
	}

	@Operation(summary = "Verify an email address (link)", description = "Link-friendly variant of email verification used by the verification email. "
			+ "The identifier and code are supplied as query parameters.", operationId = "verifyAccountByLink", tags = "Authentication")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Email address verified", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Missing parameters, or the code is invalid/expired", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/verify")
	public MessageResponse verify(
			@Parameter(description = "Email address to verify", example = "ada@example.com", required = true) @RequestParam @NotBlank String identifier,
			@Parameter(description = "Verification code from the email", example = "123456", required = true) @RequestParam @NotBlank String code) {

		return authService.verify(identifier, code);
	}

	@Operation(summary = "Resend a verification email", description = "Requires an authenticated caller. If an unverified account exists for the email, "
			+ "the previous code is replaced and a new email is sent. The response is intentionally "
			+ "generic to avoid account enumeration.", operationId = "resendVerification", tags = "Authentication", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Request accepted; the response does not reveal whether an email was sent", content = @Content(schema = @Schema(implementation = MessageResponse.class))),
			@ApiResponse(responseCode = "400", description = "Request validation failed", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Authentication is required", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/resend-verification")
	public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
		return authService.resendVerification(request.email());
	}

	private String clientIpAddress(String forwardedFor, HttpServletRequest request) {
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}

		return request.getRemoteAddr();
	}

}
