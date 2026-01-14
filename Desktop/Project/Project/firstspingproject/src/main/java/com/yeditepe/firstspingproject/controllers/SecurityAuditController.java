package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.entity.SecurityAuditLog;
import com.yeditepe.firstspingproject.service.SecurityAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Security Audit Controller
 * Provides endpoints for security monitoring and audit log management
 * All endpoints require ADMIN role
 */
@RestController
@RequestMapping("/api/admin/security")
@PreAuthorize("hasAuthority('ADMIN')")
public class SecurityAuditController {

    @Autowired
    private SecurityAuditService securityAuditService;

    /**
     * Get recent audit logs (paginated)
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<SecurityAuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(securityAuditService.getRecentLogs(page, size));
    }

    /**
     * Get audit logs for a specific user
     */
    @GetMapping("/audit-logs/user/{userId}")
    public ResponseEntity<List<SecurityAuditLog>> getUserAuditLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(securityAuditService.getUserLogs(userId));
    }

    /**
     * Get audit logs by event type
     */
    @GetMapping("/audit-logs/type/{eventType}")
    public ResponseEntity<List<SecurityAuditLog>> getAuditLogsByType(@PathVariable String eventType) {
        return ResponseEntity.ok(securityAuditService.getLogsByEventType(eventType.toUpperCase()));
    }

    /**
     * Get suspicious IPs (IPs with multiple failed login attempts)
     */
    @GetMapping("/suspicious-ips")
    public ResponseEntity<List<Map<String, Object>>> getSuspiciousIps(
            @RequestParam(defaultValue = "24") int windowHours,
            @RequestParam(defaultValue = "5") int threshold) {
        
        List<Object[]> results = securityAuditService.getSuspiciousIps(windowHours, threshold);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("ip_address", row[0]);
            entry.put("failed_attempts", row[1]);
            response.add(entry);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get security statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSecurityStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Get login statistics
        Map<String, Object> loginStats = new LinkedHashMap<>();
        loginStats.put("successful_logins", 
            securityAuditService.getLogsByEventType(SecurityAuditService.EVENT_LOGIN_SUCCESS).size());
        loginStats.put("failed_logins", 
            securityAuditService.getLogsByEventType(SecurityAuditService.EVENT_LOGIN_FAILURE).size());
        loginStats.put("token_refreshes", 
            securityAuditService.getLogsByEventType(SecurityAuditService.EVENT_TOKEN_REFRESH).size());
        loginStats.put("logouts", 
            securityAuditService.getLogsByEventType(SecurityAuditService.EVENT_LOGOUT).size());
        stats.put("login_stats", loginStats);

        // Get access denied count
        Map<String, Object> accessStats = new LinkedHashMap<>();
        accessStats.put("access_denied_count", 
            securityAuditService.getLogsByEventType(SecurityAuditService.EVENT_ACCESS_DENIED).size());
        stats.put("access_stats", accessStats);

        // Get suspicious IPs (last 24 hours, more than 5 failures)
        stats.put("suspicious_ips_count", securityAuditService.getSuspiciousIps(24, 5).size());

        return ResponseEntity.ok(stats);
    }

    /**
     * Clean up old audit logs
     */
    @DeleteMapping("/audit-logs/cleanup")
    public ResponseEntity<?> cleanupAuditLogs(@RequestParam(defaultValue = "90") int retentionDays) {
        try {
            securityAuditService.cleanupOldLogs(retentionDays);
            return ResponseEntity.ok(Map.of(
                "message", "Audit logs older than " + retentionDays + " days have been cleaned up",
                "retention_days", retentionDays
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get available event types
     */
    @GetMapping("/event-types")
    public ResponseEntity<List<String>> getEventTypes() {
        return ResponseEntity.ok(Arrays.asList(
            SecurityAuditService.EVENT_LOGIN_SUCCESS,
            SecurityAuditService.EVENT_LOGIN_FAILURE,
            SecurityAuditService.EVENT_LOGOUT,
            SecurityAuditService.EVENT_TOKEN_REFRESH,
            SecurityAuditService.EVENT_TOKEN_INVALID,
            SecurityAuditService.EVENT_ACCESS_DENIED,
            SecurityAuditService.EVENT_PASSWORD_CHANGE,
            SecurityAuditService.EVENT_PASSWORD_RESET,
            SecurityAuditService.EVENT_ACCOUNT_LOCKED,
            SecurityAuditService.EVENT_ROLE_ASSIGNED,
            SecurityAuditService.EVENT_ROLE_REMOVED,
            SecurityAuditService.EVENT_USER_DELETED
        ));
    }
}
