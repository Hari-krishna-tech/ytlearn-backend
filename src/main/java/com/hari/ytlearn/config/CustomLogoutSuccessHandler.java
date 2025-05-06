package com.hari.ytlearn.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Value("${jwt.cookie.access-token-name}")
    private String accessTokenCookieName;

    @Value("${jwt.cookie.refresh-token-name}")
    private String refreshTokenCookieName;

    @Value("${frontend.redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onLogoutSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException, ServletException {
        // Clear the JWT cookies by setting maxAge to 0
        clearCookie(response, accessTokenCookieName);
        clearCookie(response, refreshTokenCookieName);

        // Redirect to home page or login page after logout
        response.setStatus(200);
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
