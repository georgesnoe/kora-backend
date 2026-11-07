package com.kora.backend.exception;

public class InvalidTokenException extends UnauthorizedException {

	public InvalidTokenException(String message) {
		super("INVALID_TOKEN", message);
	}

}
