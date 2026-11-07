package com.kora.backend.exception;

public class VerificationException extends BadRequestException {

	public VerificationException(String message) {
		super("VERIFICATION_FAILED", message);
	}

}
