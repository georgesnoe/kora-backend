package com.kora.backend.controller;

import com.kora.backend.dto.request.RefreshTokenRequest;
import com.kora.backend.dto.request.ResendVerificationRequest;
import com.kora.backend.dto.request.SignInRequest;
import com.kora.backend.dto.request.SignOutRequest;
import com.kora.backend.dto.request.SignUpRequest;
import com.kora.backend.dto.request.VerifyAccountRequest;
import com.kora.backend.dto.response.AuthResponse;
import com.kora.backend.dto.response.MessageResponse;
import com.kora.backend.dto.response.SignUpResponse;
import com.kora.backend.security.AuthenticatedUser;
import com.kora.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/sign-up")
	public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signUp(request));
	}

	@PostMapping("/sign-in")
	public AuthResponse signIn(
			@Valid @RequestBody SignInRequest request,
			HttpServletRequest httpRequest) {

		return authService.signIn(
				request,
				clientIpAddress(httpRequest),
				httpRequest.getHeader(HttpHeaders.USER_AGENT));
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authService.refresh(request.refreshToken());
	}

	@PostMapping("/sign-out")
	public MessageResponse signOut(
			@AuthenticationPrincipal AuthenticatedUser principal,
			@RequestBody(required = false) SignOutRequest request) {

		return authService.signOut(principal, request == null ? null : request.refreshToken());
	}

	@PostMapping("/verify")
	public MessageResponse verify(@Valid @RequestBody VerifyAccountRequest request) {
		return authService.verify(request.identifier(), request.code());
	}

	@GetMapping("/verify")
	public MessageResponse verify(
			@RequestParam @NotBlank String identifier,
			@RequestParam @NotBlank String code) {

		return authService.verify(identifier, code);
	}

	@PostMapping("/resend-verification")
	public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
		return authService.resendVerification(request.email());
	}

	private String clientIpAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");

		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}

		return request.getRemoteAddr();
	}

}
