package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.entity.Role;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.EventRepository;
import com.yeditepe.firstspingproject.repository.PaymentRepository;
import com.yeditepe.firstspingproject.repository.RoleRepository;
import com.yeditepe.firstspingproject.repository.TicketRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Admin Controller
 * Provides admin-only endpoints for system management
 * All endpoints require ADMIN role
 */
@SuppressWarnings("null")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final LocalDateTime startTime = LocalDateTime.now();

    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        // User statistics
        Map<String, Object> userStats = new LinkedHashMap<>();
        userStats.put("total_users", userRepository.count());
        userStats.put("users_by_role", getUsersByRole());
        dashboard.put("users", userStats);

        // Event statistics
        Map<String, Object> eventStats = new LinkedHashMap<>();
        eventStats.put("total_events", eventRepository.count());
        dashboard.put("events", eventStats);

        // Ticket statistics
        Map<String, Object> ticketStats = new LinkedHashMap<>();
        ticketStats.put("total_tickets", ticketRepository.count());
        dashboard.put("tickets", ticketStats);

        // Payment statistics
        Map<String, Object> paymentStats = new LinkedHashMap<>();
        paymentStats.put("total_payments", paymentRepository.count());
        dashboard.put("payments", paymentStats);

        // System info
        dashboard.put("system", getSystemMetrics());
        dashboard.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get all users (admin only)
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> userList = users.stream().map(user -> {
            Map<String, Object> userMap = new LinkedHashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
            return userMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userList);
    }

    /**
     * Get user by ID (admin only)
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> userMap = new LinkedHashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("email", user.getEmail());
                    userMap.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
                    return ResponseEntity.ok(userMap);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Assign role to user
     */
    @PostMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Role role = roleRepository.findByName(roleName.toUpperCase())
                    .orElseGet(() -> {
                        Role newRole = new Role(roleName.toUpperCase());
                        return roleRepository.save(newRole);
                    });

            user.getRoles().add(role);
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Role " + roleName + " assigned to user " + user.getUsername());
            response.put("user_roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove role from user
     */
    @DeleteMapping("/users/{userId}/roles/{roleName}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Long userId, @PathVariable String roleName) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.getRoles().removeIf(role -> role.getName().equalsIgnoreCase(roleName));
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Role " + roleName + " removed from user " + user.getUsername());
            response.put("user_roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete user (admin only)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Revoke all refresh tokens for user
            refreshTokenService.revokeAllUserTokens(id);
            
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all roles
     */
    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    /**
     * Create new role
     */
    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");

            if (roleRepository.findByName(name.toUpperCase()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role already exists"));
            }

            Role role = new Role(name.toUpperCase(), description);
            Role savedRole = roleRepository.save(role);

            return ResponseEntity.ok(savedRole);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get system metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(getSystemMetrics());
    }

    /**
     * Clean up expired refresh tokens
     */
    @PostMapping("/cleanup/tokens")
    public ResponseEntity<?> cleanupExpiredTokens() {
        try {
            refreshTokenService.deleteExpiredTokens();
            return ResponseEntity.ok(Map.of("message", "Expired tokens cleaned up successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reset user password (admin only)
     */
    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newPassword = request.get("newPassword");
            if (newPassword == null || newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Revoke all refresh tokens
            refreshTokenService.revokeAllUserTokens(id);

            return ResponseEntity.ok(Map.of("message", "Password reset successfully for user: " + user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Helper methods

    private Map<String, Long> getUsersByRole() {
        Map<String, Long> roleCount = new HashMap<>();
        roleRepository.findAll().forEach(role -> {
            long count = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().contains(role))
                    .count();
            roleCount.put(role.getName(), count);
        });
        return roleCount;
    }

    private Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        // JVM metrics
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("uptime_ms", runtimeMXBean.getUptime());
        jvm.put("uptime_formatted", getUptime());
        jvm.put("java_version", System.getProperty("java.version"));

        Map<String, Object> memory = new LinkedHashMap<>();
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        memory.put("heap_used_mb", heapUsed / (1024 * 1024));
        memory.put("heap_max_mb", heapMax / (1024 * 1024));
        memory.put("heap_usage_percent", (heapUsed * 100) / heapMax);

        metrics.put("jvm", jvm);
        metrics.put("memory", memory);
        metrics.put("processors", Runtime.getRuntime().availableProcessors());

        return metrics;
    }

    private String getUptime() {
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
