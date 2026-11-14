package com.kora.backend.exception;

public class EmailAlreadyExistsException extends ConflictException {

	public EmailAlreadyExistsException(String email) {
		super("EMAIL_ALREADY_EXISTS", "An account with email '" + email + "' already exists");
	}

}
