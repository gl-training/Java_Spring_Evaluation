package com.java.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.java.model.UserInfo;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository  extends JpaRepository<UserInfo, UUID>{

	@Query("select u from UserInfo u left join fetch u.phones where u.email = :email")
	Optional<UserInfo> findByEmail(@Param("email") String email);
}
