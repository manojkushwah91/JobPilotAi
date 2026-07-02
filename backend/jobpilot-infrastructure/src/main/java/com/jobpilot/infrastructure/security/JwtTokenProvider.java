package com.jobpilot.infrastructure.security;

import com.jobpilot.application.identity.ports.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider implements TokenProvider {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    private static KeyPair devKeyPair;

    public JwtTokenProvider(
            @Value("${jwt.rsa.private-key:}") String privateKeyPath,
            @Value("${jwt.rsa.public-key:}") String publicKeyPath,
            @Value("${jwt.access-token.expiration:900000}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token.expiration:604800000}") long refreshTokenExpirationMs) {
        this.privateKey = loadPrivateKey(privateKeyPath);
        this.publicKey = loadPublicKey(publicKeyPath);
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String createAccessToken(String userId, String email, List<String> roles, String tier) {
        var now = Instant.now();
        return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .claim("roles", roles)
            .claim("tier", tier)
            .claim("type", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public String createRefreshToken(String userId, String family) {
        var now = Instant.now();
        return Jwts.builder()
            .subject(userId)
            .claim("family", family)
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("Invalid or expired token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return parseToken(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return parseToken(token).get("roles", List.class);
    }

    public String getTokenType(String token) {
        return parseToken(token).get("type", String.class);
    }

    @Override
    public Instant getExpirationFromToken(String token) {
        return parseToken(token).getExpiration().toInstant();
    }

    @Override
    public String generateAccessToken(String userId, String email, List<String> roles, String tier) {
        return createAccessToken(userId, email, roles, tier);
    }

    @Override
    public String generateRefreshToken(String userId, String family) {
        return createRefreshToken(userId, family);
    }

    private PrivateKey loadPrivateKey(String path) {
        try {
            if (path == null || path.isBlank() || path.contains("placeholder")) {
                return getDevKeyPair().getPrivate();
            }
            var keyBytes = loadKeyBytes(path);
            var kf = java.security.KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private PublicKey loadPublicKey(String path) {
        try {
            if (path == null || path.isBlank() || path.contains("placeholder")) {
                return getDevKeyPair().getPublic();
            }
            var keyBytes = loadKeyBytes(path);
            var kf = java.security.KeyFactory.getInstance("RSA");
            return kf.generatePublic(new java.security.spec.X509EncodedKeySpec(keyBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    private static KeyPair getDevKeyPair() {
        if (devKeyPair == null) {
            try {
                var generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                devKeyPair = generator.generateKeyPair();
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate dev key pair", e);
            }
        }
        return devKeyPair;
    }

    private byte[] loadKeyBytes(String path) {
        try {
            var resource = getClass().getClassLoader().getResource(path);
            if (resource != null) {
                try (var is = resource.openStream()) {
                    return is.readAllBytes();
                }
            }
            return Base64.getDecoder().decode(path);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load key from: " + path, e);
        }
    }
}
