package com.kora.backend.exception;

import com.kora.backend.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApiException(
			ApiException exception,
			HttpServletRequest request) {

		return ResponseEntity.status(exception.getStatus())
				.body(ErrorResponse.of(
						exception.getStatus(),
						exception.getCode(),
						exception.getMessage(),
						request.getRequestURI()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {

		Map<String, String> fieldErrors = new LinkedHashMap<>();

		for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
			fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}

		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(
						HttpStatus.BAD_REQUEST,
						"VALIDATION_FAILED",
						"Request validation failed",
						request.getRequestURI(),
						fieldErrors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraintViolationException(
			ConstraintViolationException exception,
			HttpServletRequest request) {

		Map<String, String> fieldErrors = new LinkedHashMap<>();

		for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
			fieldErrors.putIfAbsent(
					violation.getPropertyPath().toString(),
					violation.getMessage());
		}

		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(
						HttpStatus.BAD_REQUEST,
						"VALIDATION_FAILED",
						"Request validation failed",
						request.getRequestURI(),
						fieldErrors));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableBody(
			HttpMessageNotReadableException exception,
			HttpServletRequest request) {

		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(
						HttpStatus.BAD_REQUEST,
						"INVALID_REQUEST_BODY",
						"Request body is missing or malformed",
						request.getRequestURI()));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParameter(
			MissingServletRequestParameterException exception,
			HttpServletRequest request) {

		return ResponseEntity.badRequest()
				.body(ErrorResponse.of(
						HttpStatus.BAD_REQUEST,
						"MISSING_PARAMETER",
						exception.getMessage(),
						request.getRequestURI()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(
			AccessDeniedException exception,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(ErrorResponse.of(
						HttpStatus.FORBIDDEN,
						"ACCESS_DENIED",
						"You do not have permission to access this resource",
						request.getRequestURI()));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
			DataIntegrityViolationException exception,
			HttpServletRequest request) {

		log.warn("Data integrity violation while processing {}", request.getRequestURI(), exception);

		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(ErrorResponse.of(
						HttpStatus.CONFLICT,
						"DATA_INTEGRITY_VIOLATION",
						"The request conflicts with existing data",
						request.getRequestURI()));
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoResourceFound(
			NoResourceFoundException exception,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ErrorResponse.of(
						HttpStatus.NOT_FOUND,
						"RESOURCE_NOT_FOUND",
						"Resource not found",
						request.getRequestURI()));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(
			HttpRequestMethodNotSupportedException exception,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(ErrorResponse.of(
						HttpStatus.METHOD_NOT_ALLOWED,
						"METHOD_NOT_ALLOWED",
						exception.getMessage(),
						request.getRequestURI()));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
			HttpMediaTypeNotSupportedException exception,
			HttpServletRequest request) {

		return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
				.body(ErrorResponse.of(
						HttpStatus.UNSUPPORTED_MEDIA_TYPE,
						"UNSUPPORTED_MEDIA_TYPE",
						exception.getMessage(),
						request.getRequestURI()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedException(
			Exception exception,
			HttpServletRequest request) {

		log.error("Unhandled exception while processing {}", request.getRequestURI(), exception);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ErrorResponse.of(
						HttpStatus.INTERNAL_SERVER_ERROR,
						"INTERNAL_SERVER_ERROR",
						"An unexpected error occurred",
						request.getRequestURI()));
	}

}
