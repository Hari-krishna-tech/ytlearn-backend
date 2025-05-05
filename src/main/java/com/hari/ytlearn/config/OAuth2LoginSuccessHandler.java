package com.hari.ytlearn.config;

import com.hari.ytlearn.model.User;
import com.hari.ytlearn.service.JwtService;
import com.hari.ytlearn.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService; // To fetch the internal user ID

    @Value("${frontend.redirect-url}")
    private String frontendRedirectUrl;

    @Value("${jwt.cookie.access-token-name}")
    private String accessTokenCookieName;

    @Value("${jwt.cookie.refresh-token-name}")
    private String refreshTokenCookieName;

    @Value("${jwt.access-token.expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String googleId = oidcUser.getSubject(); // "sub" claim is the Google ID

        // Find the user in your database using the Google ID
        User user = userService.findByGoogleId(googleId);

        if (user == null) {
            // This should ideally not happen if oidcUserService ran correctly
            // Handle error appropriately - maybe redirect to an error page
            System.err.println(
                    "User not found in DB after OAuth2 login for googleId: " +
                            googleId
            );
            response.sendRedirect("/error?message=UserNotFound"); // Example error redirect
            return;
        }

        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Set cookies
        addCookie(
                response,
                accessTokenCookieName,
                accessToken,
                (int) TimeUnit.MILLISECONDS.toSeconds(accessTokenExpirationMs)
        );
        addCookie(
                response,
                refreshTokenCookieName,
                refreshToken,
                (int) TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpirationMs)
        );

        // Redirect to the frontend dashboard
        response.sendRedirect(frontendRedirectUrl);
    }

    private void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAgeSeconds
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // Make cookie available for all paths
        cookie.setHttpOnly(true); // Prevent JS access to the cookie
        cookie.setMaxAge(maxAgeSeconds);
        // cookie.setSecure(true); // Uncomment in production (HTTPS)
        response.addCookie(cookie);
    }
}
