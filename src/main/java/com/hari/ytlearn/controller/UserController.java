package com.hari.ytlearn.controller;

import com.hari.ytlearn.model.User;
import com.hari.ytlearn.service.JwtService;
import com.hari.ytlearn.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;


    @Autowired
    private UserDetailsService userDetailsService; // Use UserDetailsService


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @CookieValue(
                    name = "${jwt.cookie.access-token-name}",
                    required = false
            ) String accessToken,
            HttpServletResponse response
    ) {
        // TODO: Implement logic to get current user
        if (accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token not found in cookie.");
        }

        String userId = null;
        try {
            // Validate the refresh token itself (check signature, expiry)
            if (!jwtService.isTokenValid(accessToken)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired refresh token.");
            }
            userId = jwtService.extractUserId(accessToken);

            // Load user details using the ID from the token
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(userId);

            // Optional: Add extra validation here if needed (e.g., check if refresh token is revoked in DB)

            // Ensure the UserDetails loaded corresponds to the token's subject
            // The isTokenValid(token, userDetails) check implicitly does this if User implements UserDetails correctly
            if (!jwtService.isTokenValid(accessToken, (User) userDetails)) { // Cast might be needed
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token mismatch.");
            }


            return ResponseEntity.ok((User) userDetails);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
