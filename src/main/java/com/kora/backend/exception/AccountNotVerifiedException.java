package com.kora.backend.exception;

public class AccountNotVerifiedException extends ForbiddenException {

	public AccountNotVerifiedException() {
		super("EMAIL_NOT_VERIFIED", "Please verify your email address before signing in");
	}

}
