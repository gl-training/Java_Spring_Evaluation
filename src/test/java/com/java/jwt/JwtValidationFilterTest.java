package com.java.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtValidationFilterTest {

    // Class Under Test (CUT)
    private final JwtValidationFilter filter = new JwtValidationFilter();

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock // Mocking the log dependency if it was injected/used via a field
    private Logger log;

    // We need to inject the mock log into the private field of the filter
    // For simplicity, we assume 'log' is statically accessible or injected.
    // If 'log' is a private field, we'd use reflection or @InjectMocks.
    // For this example, we assume the log call is internal and focus on core logic.

    private final String VALID_JWT_HEADER = "Bearer valid_token";
    private final String INVALID_JWT_HEADER = "Bearer invalid_token";
    private final String VALID_USERNAME = "testuser@example.com";
    private final String VALID_ROLE = "ROLE_ADMIN"; // As hardcoded in the filter

    /**
     * Test case: No JWT header is present in the request.
     * Expected: Filter chain proceeds, and no authentication is set.
     */
    @Test
    void doFilterInternal_NoJwtHeader_ShouldProceedWithoutSettingAuth() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        // Verify filterChain.doFilter was called
        verify(filterChain, times(1)).doFilter(request, response);

        // Verify SecurityContextHolder was NOT accessed for setting authentication
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.verify(SecurityContextHolder::getContext, never());
        }
    }

    /**
     * Test case: A valid JWT header is present.
     * Expected: Authentication is set in SecurityContext, and filter chain proceeds.
     * <p>
     * Note: We must mock the static JWT library calls (Jwts, Keys) and SecurityContextHolder.
     */
    @Test
    void doFilterInternal_ValidJwtHeader_ShouldSetAuthAndProceed() throws ServletException, IOException {
        // Arrange
        when(request.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(VALID_JWT_HEADER);

        // --- Mocks for the static JWT parsing chain ---
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
             MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class);
             MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {

            // 1. Mock Key Generation
            SecretKey mockSecretKey = mock(SecretKey.class);
            mockedKeys.when(() -> Keys.hmacShaKeyFor(any(byte[].class))).thenReturn(mockSecretKey);

            // 2. Mock JWT Parser Chain (Jwts.parserBuilder().setSigningKey().build().parseClaimsJws().getBody())
            JwtParserBuilder mockParserBuilder = mock(JwtParserBuilder.class);
            io.jsonwebtoken.JwtParser mockParser = mock(io.jsonwebtoken.JwtParser.class);
            io.jsonwebtoken.Jws mockJws = mock(io.jsonwebtoken.Jws.class);
            Claims mockClaims = mock(Claims.class);

            mockedJwts.when(Jwts::parserBuilder).thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(mockSecretKey)).thenReturn(mockParserBuilder);
            when(mockParserBuilder.build()).thenReturn(mockParser);
            when(mockParser.parseClaimsJws(anyString())).thenReturn(mockJws);
            when(mockJws.getBody()).thenReturn(mockClaims);

            // 3. Mock Claims extraction
            when(mockClaims.get("username")).thenReturn(VALID_USERNAME); // Must match the key used in the filter

            // --- Mocks for SecurityContextHolder ---
            SecurityContext mockSecurityContext = mock(SecurityContext.class);
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockSecurityContext);

            // Act
            filter.doFilterInternal(request, response, filterChain);

            // Assert
            // 1. Verify authentication was set in the context
            verify(mockSecurityContext, times(1)).setAuthentication(any(Authentication.class));

            // We can also verify the Authentication object content:
            List<GrantedAuthority> expectedAuthorities = new ArrayList<>();
            expectedAuthorities.add(new SimpleGrantedAuthority(VALID_ROLE));
            Authentication expectedAuth = new UsernamePasswordAuthenticationToken(VALID_USERNAME, null, expectedAuthorities);

            verify(mockSecurityContext).setAuthentication(expectedAuth);

            // 2. Verify filterChain.doFilter was called
            verify(filterChain, times(1)).doFilter(request, response);
        }
    }

    /**
     * Test case: JWT header is present but malformed (e.g., no "Bearer " prefix or too short).
     * The original code uses 'jwt = jwt.substring(7);', which will throw an IndexOutOfBoundsException
     * if the token is shorter than 7 characters, which is caught by the filter's generic 'catch (Exception e)'.
     * Expected: BadCredentialsException is thrown.
     */
    @Test
    void doFilterInternal_MalformedJwtHeader_ShouldThrowBadCredentialsException() {
        // Arrange
        // A token shorter than "Bearer " (7 characters)
        when(request.getHeader(SecurityConstants.JWT_HEADER)).thenReturn("Bearer");

        // Act & Assert
        // The exception thrown is a BadCredentialsException because the filter catches the IndexOutOfBoundsException
        // and re-throws a BadCredentialsException.
        assertThrows(BadCredentialsException.class, () ->
                filter.doFilterInternal(request, response, filterChain)
        );

        // Verify filterChain.doFilter was NOT called
        try {
            verify(filterChain, never()).doFilter(any(), any());
        } catch (ServletException | IOException e) {
            // Should not happen
        }
    }

    /**
     * Test case: JWT parsing fails (e.g., expired, invalid signature, corrupted).
     * The original code catches any Exception from the JWT parsing and re-throws a BadCredentialsException.
     * Expected: BadCredentialsException is thrown.
     */
    @Test
    void doFilterInternal_InvalidJwtHeader_ShouldThrowBadCredentialsException() {
        // Arrange
        when(request.getHeader(SecurityConstants.JWT_HEADER)).thenReturn(INVALID_JWT_HEADER);

        // --- Mocks for the static JWT parsing chain ---
        try (MockedStatic<Keys> mockedKeys = mockStatic(Keys.class);
             MockedStatic<Jwts> mockedJwts = mockStatic(Jwts.class)) {

            // 1. Mock Key Generation
            SecretKey mockSecretKey = mock(SecretKey.class);
            mockedKeys.when(() -> Keys.hmacShaKeyFor(any(byte[].class))).thenReturn(mockSecretKey);

            // 2. Mock JWT Parser Chain to throw a JWT-specific exception (e.g., ExpiredJwtException)
            JwtParserBuilder mockParserBuilder = mock(JwtParserBuilder.class);
            io.jsonwebtoken.JwtParser mockParser = mock(io.jsonwebtoken.JwtParser.class);

            mockedJwts.when(Jwts::parserBuilder).thenReturn(mockParserBuilder);
            when(mockParserBuilder.setSigningKey(mockSecretKey)).thenReturn(mockParserBuilder);
            when(mockParserBuilder.build()).thenReturn(mockParser);

            // This is the call that will fail inside the try block
            when(mockParser.parseClaimsJws(anyString())).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

            // Act & Assert
            // The exception thrown is a BadCredentialsException because the filter catches the ExpiredJwtException
            // and re-throws a BadCredentialsException.
            assertThrows(BadCredentialsException.class, () ->
                    filter.doFilterInternal(request, response, filterChain)
            );

            // Verify filterChain.doFilter was NOT called
            try {
                verify(filterChain, never()).doFilter(any(), any());
            } catch (ServletException | IOException e) {
                // Should not happen
            }
        }
    }
}