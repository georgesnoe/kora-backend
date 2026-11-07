package com.kora.backend.security;

import com.kora.backend.exception.InvalidTokenException;
import com.kora.backend.repository.SessionRepository;
import com.kora.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";
	private static final String SESSION_ID_CLAIM = "sid";
	private static final String EMAIL_CLAIM = "email";

	private final JwtService jwtService;
	private final SessionRepository sessionRepository;

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
			String token = authorization.substring(BEARER_PREFIX.length()).trim();

			try {
				Jwt jwt = jwtService.parseAccessToken(token);
				String sessionId = jwt.getClaimAsString(SESSION_ID_CLAIM);

				if (sessionId != null && sessionRepository.existsById(sessionId)) {
					AuthenticatedUser principal = new AuthenticatedUser(
							jwt.getSubject(),
							jwt.getClaimAsString(EMAIL_CLAIM),
							sessionId);

					UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
							principal, null, List.of());
					authentication.setDetails(
							new WebAuthenticationDetailsSource().buildDetails(request));

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (InvalidTokenException exception) {
				SecurityContextHolder.clearContext();
			}
		}

		filterChain.doFilter(request, response);
	}

}
