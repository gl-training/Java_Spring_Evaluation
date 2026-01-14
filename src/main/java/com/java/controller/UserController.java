package com.java.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.java.exceptions.UserException;
import com.java.model.UserDTO;
import com.java.model.UserInfo;
import com.java.service.UserService;

@RestController
public class UserController {

	@Autowired
	private UserService userService;
	
	@PostMapping("/app/sign-up")
	public ResponseEntity<UserDTO> signUpUserHandler(@Validated @RequestBody UserDTO user) throws UserException {

		UserDTO p = userService.registerUser(user);
		
		return new ResponseEntity<UserDTO>(p,HttpStatus.CREATED);
	}

	// Authentication with JWT token
	@GetMapping("/app/login")
	public ResponseEntity<UserInfo> welcomeLoggedInUserHandler() throws UserException {
		UserInfo user =  userService.loginUser();
		return ResponseEntity.ok(user);
	}
}
