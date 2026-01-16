package com.java.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UserException.class)
	public ResponseEntity<ErrorDetails> myExceptionHandler(UserException pe , WebRequest req)
	{
		ErrorDetails err  = new ErrorDetails();
		err.setTimestamp(LocalDateTime.now());
		err.setCode(ErrorCode.ERROR_INPUT_REQUEST);
		err.setDetail(pe.getMessage());
		
		return new ResponseEntity<ErrorDetails>(err,HttpStatus.BAD_REQUEST);
		
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDetails> myExceptionHandler(MethodArgumentNotValidException pe)
	{
		ErrorDetails err  = new ErrorDetails();
		err.setTimestamp(LocalDateTime.now());
		err.setCode(ErrorCode.ERROR_SING_UP);
		err.setDetail(pe.getFieldError().getDefaultMessage());
		
		return new ResponseEntity<ErrorDetails>(err,HttpStatus.BAD_REQUEST);
		
	}
	 
	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorDetails> myExceptionHandler(BadCredentialsException e) {
	     ErrorDetails err = new ErrorDetails();
		 err.setCode(ErrorCode.INVALID_CREDENTIALS);
	     err.setTimestamp(LocalDateTime.now());
	     err.setDetail(e.getMessage());
	     
	     return new ResponseEntity<>(err, HttpStatus.UNAUTHORIZED);
	}
	 
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> myExceptionHandler(Exception e) {
		 ErrorDetails err = new ErrorDetails();
		 err.setCode(ErrorCode.INTERNAL_ERROR);
		 err.setTimestamp(LocalDateTime.now());
		 err.setDetail(e.getMessage());
		 
		 return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
	}
}
