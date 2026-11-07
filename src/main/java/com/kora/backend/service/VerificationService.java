package com.kora.backend.service;

import com.kora.backend.config.VerificationProperties;
import com.kora.backend.entity.User;
import com.kora.backend.entity.Verification;
import com.kora.backend.exception.VerificationException;
import com.kora.backend.repository.VerificationRepository;
import com.kora.backend.util.HashingUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

@Service
public class VerificationService {

	private final VerificationRepository verificationRepository;
	private final VerificationProperties properties;
	private final SecureRandom secureRandom = new SecureRandom();

	public VerificationService(
			VerificationRepository verificationRepository,
			VerificationProperties properties) {

		this.verificationRepository = verificationRepository;
		this.properties = properties;
	}

	@Transactional
	public String createVerification(User user) {
		verificationRepository.deleteByIdentifier(user.getEmail());

		String code = generateCode(properties.codeLength());
		Verification verification = Verification.builder()
				.identifier(user.getEmail())
				.value(HashingUtils.sha256Hex(code))
				.expiresAt(Instant.now().plus(properties.ttl()))
				.user(user)
				.build();

		verificationRepository.save(verification);
		return code;
	}

	@Transactional
	public User verify(String identifier, String code) {
		String normalizedIdentifier = identifier.trim().toLowerCase(Locale.ROOT);

		Verification verification = verificationRepository.findByIdentifier(normalizedIdentifier)
				.orElseThrow(() -> new VerificationException("Invalid or expired verification code"));

		if (verification.getExpiresAt().isBefore(Instant.now())) {
			verificationRepository.delete(verification);
			throw new VerificationException("Verification code has expired. Please request a new one.");
		}

		String submittedCodeHash = HashingUtils.sha256Hex(code.trim());
		if (!HashingUtils.constantTimeEquals(submittedCodeHash, verification.getValue())) {
			throw new VerificationException("Invalid or expired verification code");
		}

		User user = verification.getUser();
		user.setEmailVerified(true);
		verificationRepository.delete(verification);

		return user;
	}

	private String generateCode(int length) {
		StringBuilder code = new StringBuilder(length);

		for (int index = 0; index < length; index++) {
			code.append(secureRandom.nextInt(10));
		}

		return code.toString();
	}

}
