package com.yeditepe.firstspingproject.service;

import com.yeditepe.firstspingproject.entity.RefreshToken;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.RefreshTokenRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token Service
 * Handles refresh token creation, validation, and revocation
 */
@SuppressWarnings("null")
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration:604800000}") // Default: 7 days
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new refresh token for user
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId, String deviceInfo, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Revoke any existing tokens for this user
        refreshTokenRepository.revokeAllUserTokens(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setDeviceInfo(deviceInfo);
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Create refresh token by username
     */
    @Transactional
    public RefreshToken createRefreshToken(String username, String deviceInfo, String ipAddress) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return createRefreshToken(user.getId(), deviceInfo, ipAddress);
    }

    /**
     * Find refresh token by token string
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Verify if refresh token is valid (not expired and not revoked)
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new login request");
        }

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token was revoked. Please make a new login request");
        }

        return token;
    }

    /**
     * Revoke a refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all tokens for a user
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    /**
     * Delete all expired tokens (cleanup job)
     */
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }

    /**
     * Get refresh token duration
     */
    public Long getRefreshTokenDurationMs() {
        return refreshTokenDurationMs;
    }
}
