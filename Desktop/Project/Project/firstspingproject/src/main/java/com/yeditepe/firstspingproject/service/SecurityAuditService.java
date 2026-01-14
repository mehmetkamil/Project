package com.yeditepe.firstspingproject.service;

import com.yeditepe.firstspingproject.entity.SecurityAuditLog;
import com.yeditepe.firstspingproject.repository.SecurityAuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Security Auditing
 * Provides methods to log and query security events
 */
@SuppressWarnings("null")
@Service
public class SecurityAuditService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAuditService.class);

    // Event type constants
    public static final String EVENT_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String EVENT_LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String EVENT_LOGOUT = "LOGOUT";
    public static final String EVENT_TOKEN_REFRESH = "TOKEN_REFRESH";
    public static final String EVENT_TOKEN_INVALID = "TOKEN_INVALID";
    public static final String EVENT_ACCESS_DENIED = "ACCESS_DENIED";
    public static final String EVENT_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String EVENT_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String EVENT_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    public static final String EVENT_ROLE_ASSIGNED = "ROLE_ASSIGNED";
    public static final String EVENT_ROLE_REMOVED = "ROLE_REMOVED";
    public static final String EVENT_USER_DELETED = "USER_DELETED";

    @Autowired
    private SecurityAuditLogRepository auditLogRepository;

    /**
     * Log a security event (async for performance)
     */
    @Async
    public void logEvent(SecurityAuditLog.Builder builder) {
        try {
            SecurityAuditLog log = builder.build();
            auditLogRepository.save(log);
            logger.debug("Security event logged: {} for user: {}", log.getEventType(), log.getUsername());
        } catch (Exception e) {
            logger.error("Failed to log security event: {}", e.getMessage());
        }
    }

    /**
     * Log login success
     */
    public void logLoginSuccess(Long userId, String username, HttpServletRequest request) {
        logEvent(SecurityAuditLog.builder()
                .eventType(EVENT_LOGIN_SUCCESS)
                .userId(userId)
                .username(username)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .resourcePath(request.getRequestURI())
                .successful(true));
    }

    /**
     * Log login failure
     */
    public void logLoginFailure(String username, String reason, HttpServletRequest request) {
        logEvent(SecurityAuditLog.builder()
                .eventType(EVENT_LOGIN_FAILURE)
                .username(username)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .resourcePath(request.getRequestURI())
                .details(reason)
                .successful(false));
    }

    /**
     * Log logout
     */
    public void logLogout(Long userId, String username, HttpServletRequest request) {
        logEvent(SecurityAuditLog.builder()
                .eventType(EVENT_LOGOUT)
                .userId(userId)
                .username(username)
                .ipAddress(getClientIp(request))
                .resourcePath(request.getRequestURI())
                .successful(true));
    }

    /**
     * Log token refresh
     */
    public void logTokenRefresh(Long userId, String username, HttpServletRequest request) {
        logEvent(SecurityAuditLog.builder()
                .eventType(EVENT_TOKEN_REFRESH)
                .userId(userId)
                .username(username)
                .ipAddress(getClientIp(request))
                .resourcePath(request.getRequestURI())
                .successful(true));
    }

    /**
     * Log access denied
     */
    public void logAccessDenied(String username, String resource, HttpServletRequest request) {
        logEvent(SecurityAuditLog.builder()
                .eventType(EVENT_ACCESS_DENIED)
                .username(username)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .resourcePath(resource)
                .httpMethod(request.getMethod())
                .successful(false));
    }

    /**
     * Check if IP is blocked (too many failed attempts)
     */
    public boolean isIpBlocked(String ipAddress, int maxAttempts, int windowMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);
        long failures = auditLogRepository.countLoginFailuresByIp(ipAddress, since);
        return failures >= maxAttempts;
    }

    /**
     * Get failed login attempts for username
     */
    public int getFailedLoginAttempts(String username, int windowMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);
        return auditLogRepository.findFailedLoginAttempts(username, since).size();
    }

    /**
     * Get recent audit logs (paginated)
     */
    public Page<SecurityAuditLog> getRecentLogs(int page, int size) {
        return auditLogRepository.findByOrderByTimestampDesc(PageRequest.of(page, size));
    }

    /**
     * Get logs for a specific user
     */
    public List<SecurityAuditLog> getUserLogs(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    /**
     * Get logs by event type
     */
    public List<SecurityAuditLog> getLogsByEventType(String eventType) {
        return auditLogRepository.findByEventType(eventType);
    }

    /**
     * Get suspicious IPs
     */
    public List<Object[]> getSuspiciousIps(int windowHours, int threshold) {
        LocalDateTime since = LocalDateTime.now().minusHours(windowHours);
        return auditLogRepository.findSuspiciousIps(since, threshold);
    }

    /**
     * Clean up old audit logs
     */
    @Transactional
    public void cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        auditLogRepository.deleteByTimestampBefore(cutoff);
        logger.info("Cleaned up audit logs older than {} days", retentionDays);
    }

    /**
     * Get client IP address from request
     */
    public static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
