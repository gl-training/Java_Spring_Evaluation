package com.java.jwt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.exceptions.ErrorCode;
import com.java.exceptions.ErrorDetails;
import com.java.exceptions.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtValidationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException, AuthenticationException {
	
		log.info("inside JWT validation filter.");
		String jwt= request.getHeader(SecurityConstants.JWT_HEADER);
		if(jwt != null) {
			try {
				//extracting the word Bearer
				jwt = jwt.substring(7);
				SecretKey key= Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes());
				Claims claims= Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jwt).getBody();

				String username= String.valueOf(claims.get("username"));

				// TODO: Get role from User
				String role= "ROLE_ADMIN";
				
				List<GrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority(role));
				
				Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);

				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception e) {
			    throw new BadCredentialsException("Invalid JWT Token received..", e);
			}

		}
		filterChain.doFilter(request, response);

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getServletPath().equals("/app/sign-up");
	}

}
