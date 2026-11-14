package com.kora.backend.exception;

import org.springframework.http.HttpStatus;

public class EmailSendingException extends ApiException {

	public EmailSendingException(String message, Throwable cause) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SENDING_FAILED", message);
		initCause(cause);
	}

}
