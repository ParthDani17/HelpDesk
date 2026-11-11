package com.helpdesk.helpdesk.refreshToken.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import com.helpdesk.helpdesk.refreshToken.repository.RefreshTokenRepository;
import com.helpdesk.helpdesk.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import com.helpdesk.helpdesk.refreshToken.entity.RefreshToken;
import com.helpdesk.helpdesk.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final long refreshTokenExpiration;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Transactional
    public String createRefreshToken(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String refreshToken = generateRefreshToken();

        String tokenHash = hashToken(refreshToken);

        Instant expiresAt = Instant.now().plusMillis(refreshTokenExpiration);

        RefreshToken refreshTokenEntity = new RefreshToken(
                user,
                tokenHash,
                expiresAt
        );

        refreshTokenRepository.save(refreshTokenEntity);

        return refreshToken;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {

        String tokenHash = hashToken(token);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        Instant now = Instant.now();

        if (!refreshToken.isUsable(now)) {
            throw new IllegalArgumentException("Refresh token is expired or revoked");
        }

        if (!refreshToken.getUser().isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshTokenRotationResult rotateRefreshToken(String oldToken) {

        String oldTokenHash = hashToken(oldToken);

        RefreshToken oldRefreshToken = refreshTokenRepository
                .findByTokenHashForUpdate(oldTokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token"));

        Instant now = Instant.now();

        if (!oldRefreshToken.isUsable(now)) {
            throw new IllegalArgumentException(
                    "Refresh token is expired or revoked"
            );
        }

        User user = oldRefreshToken.getUser();

        if (!user.isActive()) {
            throw new IllegalArgumentException("User account is inactive");
        }

        oldRefreshToken.revoke(now);

        String newRefreshToken = generateRefreshToken();

        String newTokenHash = hashToken(newRefreshToken);

        Instant newExpiresAt = now.plusMillis(refreshTokenExpiration);

        RefreshToken newRefreshTokenEntity = new RefreshToken(
                user,
                newTokenHash,
                newExpiresAt
        );

        refreshTokenRepository.save(newRefreshTokenEntity);

        return new RefreshTokenRotationResult(
                newRefreshToken,
                user.getId(),
                user.getRole()
        );
    }

    public String generateRefreshToken() {

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    public String hashToken(String token) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = messageDigest.digest(
                    token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
        }
    }
}