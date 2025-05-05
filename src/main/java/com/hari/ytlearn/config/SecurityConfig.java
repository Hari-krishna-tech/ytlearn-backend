package com.hari.ytlearn.config;

import com.hari.ytlearn.model.OAuthToken;
import com.hari.ytlearn.model.User;
import com.hari.ytlearn.repository.OAuthTokenRepository;
import com.hari.ytlearn.service.OAuthTokenService;
import com.hari.ytlearn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private OAuthTokenService oAuthTokenService;
    @Autowired

    private UserService userService;


    // Inject ClientRegistrationRepository here
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http
                                                   ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error", "login/**", "/oauth2/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Configure OAuth2 login - note the proper nesting
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/api/playlists", true)
                        // Configure the authorization endpoint
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestResolver(
                                        authorizationRequestResolver(
                                                this.clientRegistrationRepository
                                        ) // Wire the custom resolver here
                                )
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService())
                        )
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/").permitAll());

        return http.build();
    }

    /**
     * Creates the custom AuthorizationRequestResolver Bean.
     * This resolver adds 'access_type=offline' to the authorization request
     * for Google, which is necessary to request a refresh token.
     */
    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository
    ) {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization"
                );

        // Return the anonymous class implementation directly
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(
                    HttpServletRequest request
            ) {
                OAuth2AuthorizationRequest req = defaultResolver.resolve(
                        request
                );
                return customizeRequest(req, extractRegistrationId(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(
                    HttpServletRequest request,
                    String clientRegistrationId
            ) {
                OAuth2AuthorizationRequest req = defaultResolver.resolve(
                        request,
                        clientRegistrationId
                );
                return customizeRequest(req, clientRegistrationId);
            }

            private OAuth2AuthorizationRequest customizeRequest(
                    OAuth2AuthorizationRequest req,
                    String registrationId
            ) {
                if (req == null) {
                    return null;
                }
                // Only add the parameter for the "google" registration
                if ("google".equalsIgnoreCase(registrationId)) {
                    Map<String, Object> additionalParams = new HashMap<>(
                            req.getAdditionalParameters()
                    );
                    // Add access_type=offline to request offline access (refresh token)
                    additionalParams.put("access_type", "offline");
                    // Optionally, force the consent screen every time (useful for testing)
                    // additionalParams.put("prompt", "consent");
                    // disable later
                    return OAuth2AuthorizationRequest
                            .from(req)
                            .additionalParameters(additionalParams)
                            .build();
                }
                return req;
            }

            private String extractRegistrationId(HttpServletRequest request) {
                // Simple extraction assuming default pattern /oauth2/authorization/{registrationId}
                String requestUri = request.getRequestURI();
                if (
                        requestUri == null ||
                                !requestUri.startsWith("/oauth2/authorization/")
                ) {
                    return null; // Or handle differently if needed
                }
                String[] parts = requestUri.split("/");
                return (parts.length > 3) ? parts[3] : null;
            }
        };
    }


    /**
     * Customized OIDC user service that extracts user details and OAuth tokens
     * from the authentication response and persists them using service classes.
     */
    @Bean
    public OidcUserService oidcUserService() {
        OidcUserService delegate = new OidcUserService();

        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                // Load the user using the delegate service
                OidcUser oidcUser = delegate.loadUser(userRequest);

                try {
                    // Extract user information from claims
                    Map<String, Object> claims = oidcUser.getClaims();

                    // Extract required user details
                    String googleId = (String) claims.get("sub");
                    String email = (String) claims.get("email");
                    String name = (String) claims.get("name");
                    String picture = (String) claims.get("picture");

                    // Save or update user in database
                    User user = userService.findByGoogleId(googleId) ;
                    if(user == null) {
                        user = new User();
                    }

                    user.setGoogleId(googleId);
                    user.setEmail(email);
                    user.setName(name);
                    user.setProfilePicture(picture);

                    User savedUser = userService.saveUser(user);

                    // Extract token information
                    OAuth2AccessToken accessToken = userRequest.getAccessToken();


                    String scope = accessToken.getScopes().toString();
                    OAuthToken existingToken = oAuthTokenService.getTokenByUserId(savedUser.getId());
                    OAuthToken newToken;
                    if(existingToken != null) {

                        newToken = existingToken;
                        newToken.setUser(savedUser);
                        newToken.setUpdatedAt(Instant.now());
                    } else {
                        newToken = new OAuthToken();
                    }

                    String tokenValue = accessToken.getTokenValue();
                    String tokenType = accessToken.getTokenType().getValue();
                    Instant expiresAt = accessToken.getExpiresAt();
                    newToken.setAccessToken(tokenValue);
                    newToken.setTokenType(tokenType);
                    newToken.setExpiresAt(expiresAt);
                    newToken.setScope(scope);

                    // Get refresh token if available
                    // Note: Refresh token is typically only sent on initial authentication

                    // Get refresh token if available
                    String refreshToken = null;
                    if (
                            userRequest
                                    .getAdditionalParameters()
                                    .containsKey("refresh_token")
                    ) {
                        refreshToken =
                                (String) userRequest
                                        .getAdditionalParameters()
                                        .get("refresh_token");
                        System.out.println("Refresh Token Found!"); // Add logging
                    } else {
                        System.out.println(
                                "Refresh Token not found in additionalParameters."
                        ); // Add logging
                    }

                    if (refreshToken != null) {
                        newToken.setRefreshToken(refreshToken);
                    }
                    // Save token information
                    oAuthTokenService.save(newToken);

                    // Log successful authentication
                    System.out.println("Successfully authenticated user: " + email);

                } catch (Exception e) {
                    // Log any errors but don't prevent authentication
                    System.err.println("Error processing OAuth2 user: " + e.getMessage());
                    e.printStackTrace();
                }

                // Return the original OidcUser to continue the authentication process
                return oidcUser;
            }
        };
    }


    /**
     * Optional: Customize the OIDC user service to process and adapt the user details.
     * For example, you can map additional attributes or perform actions when users log in.
     */

}
