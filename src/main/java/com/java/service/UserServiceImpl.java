package com.java.service;

import com.java.model.PhoneDTO;
import com.java.model.PhoneInfo;
import com.java.utils.EncryptionUtil;
import com.java.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.java.exceptions.UserException;
import com.java.model.UserDTO;
import com.java.model.UserInfo;
import com.java.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private EncryptionUtil encryptionUtil;

	@Override
	public UserDTO registerUser(UserDTO user) throws UserException {
 
		UserInfo findUser = userRepo.findByEmail(user.getEmail());
		
		if(findUser!=null) {
			throw new UserException("User already exist with email: "+user.getEmail());
		}

		UserInfo newUser = getUserInfo(user);
		newUser.setPassword(encryptionUtil.encrypt(newUser.getPassword()));

		newUser.setToken(jwtUtil.generateToken(user.getEmail()));
		newUser.setIsActive(true);

		return getUserResponse(userRepo.save(newUser));
	}

	private static UserDTO getUserResponse(UserInfo userInfo) {
		UserDTO userResponse = new UserDTO();
		userResponse.setId(String.valueOf(userInfo.getId()));
		userResponse.setCreated(userInfo.getCreated());
		userResponse.setLastLogin(userInfo.getLastLogin());
		userResponse.setToken(userInfo.getToken());
		userResponse.setIsActive(userInfo.getIsActive());
		return userResponse;
	}

	private static UserInfo getUserInfo(UserDTO user) {
		UserInfo newUser = new UserInfo();

		newUser.setEmail(user.getEmail());
		newUser.setName(user.getName());
		newUser.setPassword(user.getPassword());

		if (!user.getPhones().isEmpty()){
			List<PhoneInfo> phones = new ArrayList<>();
			for(PhoneDTO phoneDTO: user.getPhones()){
				PhoneInfo phoneInfo = new PhoneInfo();
				phoneInfo.setCityCode(phoneDTO.getCityCode());
				phoneInfo.setNumber(phoneDTO.getNumber());
				phoneInfo.setCountryCode(phoneDTO.getCountryCode());
				phones.add(phoneInfo);
			}
			newUser.setPhones(phones);
		}
		return newUser;
	}

	@Override
	public UserInfo loginUser() {
			
		SecurityContext sc  = SecurityContextHolder.getContext();
		Authentication auth  = sc.getAuthentication();
		String userName = auth.getName();
		UserInfo user = userRepo.findByEmail(userName);

		// Update Token and Last Login Date
		user.setToken(jwtUtil.generateToken(user.getEmail()));
		user.setLastLogin(LocalDateTime.now());

		UserInfo userResponse = userRepo.save(user);
		userResponse.setPassword(encryptionUtil.decrypt(userResponse.getPassword()));

		return userResponse;
	}
	
}
