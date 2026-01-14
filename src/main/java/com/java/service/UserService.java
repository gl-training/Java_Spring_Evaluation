package com.java.service;

import com.java.exceptions.UserException;
import com.java.model.UserDTO;
import com.java.model.UserInfo;

public interface UserService {

	public UserDTO registerUser(UserDTO user) throws UserException;
	public UserInfo loginUser()  throws UserException;
}
