package com.java.exceptions;

import java.time.LocalDateTime;
import java.util.Collections;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorResponse> myExceptionHandler(UserException pe , WebRequest req)
	{
		ErrorDetails err  = new ErrorDetails();
		err.setTimestamp(LocalDateTime.now());
		err.setCode(ErrorCode.ERROR_INPUT_REQUEST);
		err.setDetail(pe.getMessage());

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setError(Collections.singletonList(err));

		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> myExceptionHandler(MethodArgumentNotValidException pe)
	{
		ErrorDetails err  = new ErrorDetails();
		err.setTimestamp(LocalDateTime.now());
		err.setCode(ErrorCode.ERROR_SING_UP);
		err.setDetail(pe.getFieldError().getDefaultMessage());

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setError(Collections.singletonList(err));
		
		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.UNAUTHORIZED);
	}
	 
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> myExceptionHandler(BadCredentialsException e) {
	     ErrorDetails err = new ErrorDetails();
		 err.setCode(ErrorCode.INVALID_CREDENTIALS);
	     err.setTimestamp(LocalDateTime.now());
	     err.setDetail(e.getMessage());

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setError(Collections.singletonList(err));

		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.UNAUTHORIZED);
	}
	 
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> myExceptionHandler(Exception e) {
		 ErrorDetails err = new ErrorDetails();
		 err.setCode(ErrorCode.INTERNAL_ERROR);
		 err.setTimestamp(LocalDateTime.now());
		 err.setDetail(e.getMessage());

		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setError(Collections.singletonList(err));

		return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
