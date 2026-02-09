package com.java.jwt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import javax.crypto.SecretKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.java.exceptions.ErrorCode;
import com.java.exceptions.ErrorDetails;
import com.java.exceptions.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
			    //throw new BadCredentialsException("Invalid JWT Token received..", e);

				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());

				ErrorDetails err = new ErrorDetails();
				err.setCode(ErrorCode.ERROR_UNAUTHORIZED);
				err.setTimestamp(LocalDateTime.now());
				err.setDetail(e.getMessage());
				ErrorResponse errorResponse = new ErrorResponse();
				errorResponse.setError(Collections.singletonList(err));

				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				mapper.writeValue(response.getWriter(), errorResponse);
			}

		}
		filterChain.doFilter(request, response);

	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		return request.getServletPath().equals("/sign-up");
	}

}
