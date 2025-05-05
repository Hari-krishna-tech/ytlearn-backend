package com.hari.ytlearn.controller;

import com.hari.ytlearn.model.User;
import com.hari.ytlearn.service.JwtService;
import com.hari.ytlearn.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService; // Use UserDetailsService

    @Autowired // Keep UserService if needed for other operations or direct User object access
    private UserService userService;

    @Value("${jwt.cookie.access-token-name}")
    private String accessTokenCookieName;

    @Value("${jwt.cookie.refresh-token-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.access-token.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
        @CookieValue(
            name = "${jwt.cookie.refresh-token-name}",
            required = false
        ) String refreshToken,
        HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Refresh token not found in cookie.");
        }

        String userId = null;
        try {
            // Validate the refresh token itself (check signature, expiry)
            if (!jwtService.isTokenValid(refreshToken)) {
                 return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired refresh token.");
            }
            userId = jwtService.extractUserId(refreshToken);

            // Load user details using the ID from the token
            UserDetails userDetails =
                userDetailsService.loadUserByUsername(userId);

            // Optional: Add extra validation here if needed (e.g., check if refresh token is revoked in DB)

            // Ensure the UserDetails loaded corresponds to the token's subject
            // The isTokenValid(token, userDetails) check implicitly does this if User implements UserDetails correctly
             if (!jwtService.isTokenValid(refreshToken, (User)userDetails)) { // Cast might be needed
                 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token mismatch.");
             }


            // Generate new tokens
            String newAccessToken = jwtService.generateAccessToken(
                (User) userDetails
            ); // Cast might be needed
            String newRefreshToken = jwtService.generateRefreshToken(
                (User) userDetails
            ); // Cast might be needed

            // Set new cookies
            addCookie(
                response,
                accessTokenCookieName,
                newAccessToken,
                (int) TimeUnit.MILLISECONDS.toSeconds(
                    accessTokenExpirationMs
                )
            );
            addCookie(
                response,
                refreshTokenCookieName,
                newRefreshToken,
                (int) TimeUnit.MILLISECONDS.toSeconds(
                    refreshTokenExpirationMs
                )
            );

            System.out.println("Tokens refreshed for user ID: " + userId);
            return ResponseEntity.ok("Tokens refreshed successfully.");

        } catch (UsernameNotFoundException e) {
             System.err.println("User not found for refresh token with ID: " + userId);
             clearCookie(response, refreshTokenCookieName); // Clear invalid cookie
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        } catch (Exception e) {
            System.err.println("Error during token refresh: " + e.getMessage());
            e.printStackTrace();
             clearCookie(response, refreshTokenCookieName); // Clear potentially problematic cookie
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Token refresh failed.");
        }
    }

    private void addCookie(
        HttpServletResponse response,
        String name,
        String value,
        int maxAgeSeconds
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAgeSeconds);
        // cookie.setSecure(true); // Uncomment in production (HTTPS)
        response.addCookie(cookie);
    }

     private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null); // Set value to null
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Expire the cookie immediately
        // cookie.setSecure(true); // Uncomment in production (HTTPS)
        response.addCookie(cookie);
    }
}
