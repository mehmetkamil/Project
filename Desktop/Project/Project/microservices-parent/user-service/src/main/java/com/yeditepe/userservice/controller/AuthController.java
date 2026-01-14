package com.yeditepe.userservice.controller;

import com.yeditepe.userservice.dto.LoginRequest;
import com.yeditepe.userservice.dto.LoginResponse;
import com.yeditepe.userservice.dto.RegisterRequest;
import com.yeditepe.userservice.entity.RefreshToken;
import com.yeditepe.userservice.entity.Role;
import com.yeditepe.userservice.entity.User;
import com.yeditepe.userservice.repository.UserRepository;
import com.yeditepe.userservice.security.JwtUtil;
import com.yeditepe.userservice.service.AuthService;
import com.yeditepe.userservice.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            LoginResponse response = authService.register(request);
            
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
            
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
            String deviceInfo = httpRequest.getHeader("User-Agent");
            String ipAddress = getClientIP(httpRequest);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), deviceInfo, ipAddress);
            
            response.setRefreshToken(refreshToken.getToken());
            response.setExpiresIn(jwtExpiration);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                User user = userRepository.findByUsername(username).orElse(null);
                if (user != null) {
                    refreshTokenService.revokeAllUserTokens(user.getId());
                }
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

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, String> request) {
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

            refreshTokenService.revokeAllUserTokens(user.getId());

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

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
