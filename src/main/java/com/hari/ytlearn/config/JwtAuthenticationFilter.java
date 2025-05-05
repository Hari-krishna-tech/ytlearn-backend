package com.hari.ytlearn.config;

import com.hari.ytlearn.model.User; // Assuming User implements UserDetails or you adapt
import com.hari.ytlearn.service.JwtService;
import com.hari.ytlearn.service.UserService; // Or a UserDetailsService implementation
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // Import UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    // Inject UserDetailsService - Your UserService might need to implement it
    // or you create a separate UserDetailsServiceImpl
    @Autowired
    private UserDetailsService userDetailsService; // Use UserDetailsService

    @Value("${jwt.cookie.access-token-name}")
    private String accessTokenCookieName;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String accessToken = getCookieValue(
            request,
            accessTokenCookieName
        );
        final String userId;

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userId = jwtService.extractUserId(accessToken);
        } catch (Exception e) {
            // Handle invalid token (e.g., expired, malformed)
            System.err.println("Invalid JWT Token: " + e.getMessage());
            // Optionally send a 401 Unauthorized response here if needed,
            // but often just letting the request proceed without authentication
            // is sufficient if subsequent authorization rules handle it.
            SecurityContextHolder.clearContext(); // Ensure context is cleared
            filterChain.doFilter(request, response);
            return;
        }

        // Check if user ID is present and context has no authentication yet
        if (
            userId != null &&
            SecurityContextHolder.getContext().getAuthentication() == null
        ) {
            // Load UserDetails (Spring Security's representation of a user)
            // Your User model might need to implement UserDetails, or you need a mapping service
            UserDetails userDetails =
                this.userDetailsService.loadUserByUsername(userId); // Load by internal ID

            // Validate token against UserDetails
            if (jwtService.isTokenValid(accessToken, (User) userDetails)) { // Cast might be needed depending on your UserDetails impl
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No credentials needed for JWT
                        userDetails.getAuthorities()
                    );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource()
                        .buildDetails(request)
                );
                // Set authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                 System.err.println("JWT Token is invalid or expired for user ID: " + userId);
                 SecurityContextHolder.clearContext(); // Ensure context is cleared
            }
        } else if (userId != null) {
             // Token was present, user ID extracted, but context already had authentication.
             // This might happen in some filter chain configurations. Usually okay.
             // System.out.println("Security context already has authentication for user ID: " + userId);
        }


        filterChain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest req, String cookieName) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
