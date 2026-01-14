package com.java.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.java.model.UserInfo;

@Repository
public interface UserRepository  extends JpaRepository<UserInfo, Integer>{

	UserInfo findByEmail(String username);

}
