package com.kora.backend.exception;

public class InvalidCredentialsException extends UnauthorizedException {

	public InvalidCredentialsException() {
		super("INVALID_CREDENTIALS", "Invalid email or password");
	}

}
