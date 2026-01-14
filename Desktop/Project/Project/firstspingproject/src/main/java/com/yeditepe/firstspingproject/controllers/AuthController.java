package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.dto.LoginRequest;
import com.yeditepe.firstspingproject.dto.LoginResponse;
import com.yeditepe.firstspingproject.dto.RegisterRequest;
import com.yeditepe.firstspingproject.dto.TokenRefreshRequest;
import com.yeditepe.firstspingproject.dto.TokenRefreshResponse;
import com.yeditepe.firstspingproject.entity.RefreshToken;
import com.yeditepe.firstspingproject.entity.Role;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.security.JwtUtil;
import com.yeditepe.firstspingproject.service.AuthService;
import com.yeditepe.firstspingproject.service.RefreshTokenService;
import com.yeditepe.firstspingproject.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityAuditService securityAuditService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = authService.register(request);
            
            // Create refresh token
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIP(httpRequest);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), deviceInfo, ipAddress);
            
            response.setRefreshToken(refreshToken.getToken());
            response.setExpiresIn(jwtExpiration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Registration failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = authService.login(request);
            
            // Create refresh token
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIP(httpRequest);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), deviceInfo, ipAddress);
            
            response.setRefreshToken(refreshToken.getToken());
            response.setExpiresIn(jwtExpiration);
            
            // Log successful login
            securityAuditService.logLoginSuccess(user.getId(), user.getUsername(), httpRequest);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log failed login
            securityAuditService.logLoginFailure(request.getUsername(), e.getMessage(), httpRequest);
            
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request, HttpServletRequest httpRequest) {
        try {
            String requestRefreshToken = request.getRefreshToken();

            RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            // Verify token is valid
            refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();
            
            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(user.getUsername());

            // Log token refresh
            securityAuditService.logTokenRefresh(user.getId(), user.getUsername(), httpRequest);

            TokenRefreshResponse response = new TokenRefreshResponse(
                    newAccessToken,
                    requestRefreshToken,
                    jwtExpiration
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token refresh failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody(required = false) TokenRefreshRequest request,
                                    @RequestHeader(value = "Authorization", required = false) String authHeader,
                                    HttpServletRequest httpRequest) {
        try {
            String username = null;
            Long userId = null;
            
            // Get username from JWT and revoke all tokens
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                username = jwtUtil.extractUsername(token);
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    userId = user.getId();
                    refreshTokenService.revokeAllUserTokens(user.getId());
                }
            }
            
            // Revoke specific refresh token if provided
            if (request != null && request.getRefreshToken() != null) {
                refreshTokenService.revokeToken(request.getRefreshToken());
            }

            // Log logout
            if (userId != null && username != null) {
                securityAuditService.logLogout(userId, username, httpRequest);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Logout failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid authorization header");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get current user");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid authorization header");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Change password for current user
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, String> request,
                                           HttpServletRequest httpRequest) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid authorization header");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                throw new RuntimeException("Current password and new password are required");
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }

            if (newPassword.length() < 6) {
                throw new RuntimeException("New password must be at least 6 characters");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Revoke all refresh tokens for security
            refreshTokenService.revokeAllUserTokens(user.getId());

            // Log password change
            securityAuditService.logEvent(
                com.yeditepe.firstspingproject.entity.SecurityAuditLog.builder()
                    .eventType(SecurityAuditService.EVENT_PASSWORD_CHANGE)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .ipAddress(getClientIP(httpRequest))
                    .successful(true)
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password changed successfully. Please login again.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Password change failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
