package com.java.utils;

import com.java.jwt.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@Component
public class JwtUtil {

    public String generateToken(String email) {

        SecretKey key = Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes());
        return Jwts
                .builder()
                .setClaims(new HashMap<>())
                .setSubject(email)
                .claim("username", email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 hours token validity
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
