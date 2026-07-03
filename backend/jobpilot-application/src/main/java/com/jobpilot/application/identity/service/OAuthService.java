package com.jobpilot.application.identity.service;

import com.jobpilot.application.identity.dto.AuthResponse;
import com.jobpilot.application.identity.dto.OAuthCommand;
import com.jobpilot.application.identity.ports.TokenProvider;
import com.jobpilot.application.identity.ports.UserRepository;
import com.jobpilot.common.exception.UnauthorizedException;
import com.jobpilot.domain.identity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthService.class);
    private static final RestTemplate rest = new RestTemplate();

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Value("${oauth.google.client-id:}") private String googleClientId;
    @Value("${oauth.google.client-secret:}") private String googleClientSecret;
    @Value("${oauth.github.client-id:}") private String githubClientId;
    @Value("${oauth.github.client-secret:}") private String githubClientSecret;
    @Value("${app.base-url:http://localhost:3000}") private String appBaseUrl;

    public OAuthService(UserRepository userRepository, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }

    public String initiateOAuth(String provider) {
        var redirectUri = appBaseUrl + "/auth/callback?provider=" + provider;
        return switch (provider.toLowerCase()) {
            case "google" -> "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + googleClientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=openid%20email%20profile" +
                "&access_type=offline";
            case "github" -> "https://github.com/login/oauth/authorize?" +
                "client_id=" + githubClientId +
                "&redirect_uri=" + redirectUri +
                "&scope=user:email";
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }

    @SuppressWarnings("unchecked")
    public AuthResponse handleOAuthCallback(OAuthCommand command) {
        var tokenUrl = switch (command.provider().toLowerCase()) {
            case "google" -> "https://oauth2.googleapis.com/token";
            case "github" -> "https://github.com/login/oauth/access_token";
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + command.provider());
        };

        var body = new LinkedMultiValueMap<String, String>();
        body.add("code", command.code());
        body.add("client_id", "google".equals(command.provider()) ? googleClientId : githubClientId);
        body.add("client_secret", "google".equals(command.provider()) ? googleClientSecret : githubClientSecret);
        body.add("redirect_uri", command.redirectUri());
        body.add("grant_type", "authorization_code");

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        var tokenResponse = rest.exchange(tokenUrl, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new UnauthorizedException("Failed to exchange OAuth code for token");
        }
        var accessToken = (String) tokenResponse.getBody().get("access_token");

        var userInfoUrl = switch (command.provider().toLowerCase()) {
            case "google" -> "https://www.googleapis.com/oauth2/v3/userinfo";
            case "github" -> "https://api.github.com/user";
            default -> throw new IllegalArgumentException("Unsupported provider");
        };

        var userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        var userResponse = rest.exchange(userInfoUrl, HttpMethod.GET, new HttpEntity<>(userHeaders), Map.class);
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new UnauthorizedException("Failed to fetch user info from OAuth provider");
        }

        var info = userResponse.getBody();
        var providerUserId = switch (command.provider().toLowerCase()) {
            case "google" -> (String) info.get("sub");
            case "github" -> String.valueOf(info.get("id"));
            default -> throw new IllegalArgumentException("Unsupported provider");
        };
        var email = switch (command.provider().toLowerCase()) {
            case "google" -> (String) info.get("email");
            case "github" -> fetchGitHubEmail(accessToken);
            default -> throw new IllegalArgumentException("Unsupported provider");
        };

        var user = findOrCreateUser(OAuthProvider.Provider.valueOf(command.provider().toUpperCase()), providerUserId, email);
        return generateAuthResponse(user);
    }

    private String fetchGitHubEmail(String accessToken) {
        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        var response = rest.exchange("https://api.github.com/user/emails", HttpMethod.GET, new HttpEntity<>(headers), List.class);
        if (response.getBody() != null && !response.getBody().isEmpty()) {
            @SuppressWarnings("unchecked")
            var first = (Map<String, Object>) ((List<?>) response.getBody()).get(0);
            return (String) first.get("email");
        }
        throw new UnauthorizedException("No email found from GitHub");
    }

    private User findOrCreateUser(OAuthProvider.Provider provider, String providerUserId, String email) {
        var emailVo = Email.from(email);
        var existing = userRepository.findByEmail(emailVo);
        if (existing.isPresent()) {
            var user = existing.get();
            user.addOAuthProvider(OAuthProvider.from(provider, providerUserId));
            return userRepository.save(user);
        }
        var passwordHash = PasswordHash.from(UUID.randomUUID().toString());
        var user = User.register(emailVo, passwordHash);
        user.verifyEmail();
        user.addOAuthProvider(OAuthProvider.from(provider, providerUserId));
        return userRepository.save(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        var accessToken = tokenProvider.generateAccessToken(
            user.userId().value().toString(), user.email().value(),
            List.of(user.role().name()), user.role().name());
        var refreshToken = tokenProvider.generateRefreshToken(
            user.userId().value().toString(), user.userId().value().toString());
        var expiresIn = tokenProvider.getExpirationFromToken(accessToken).toEpochMilli();
        var userResp = new AuthResponse.UserResponse(
            user.userId().value().toString(), user.email().value(),
            user.role().name(), user.isEmailVerified());
        return AuthResponse.of(accessToken, refreshToken, expiresIn, userResp);
    }
}
