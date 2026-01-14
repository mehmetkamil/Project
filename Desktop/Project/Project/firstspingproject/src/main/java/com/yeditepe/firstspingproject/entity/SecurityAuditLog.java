package com.yeditepe.firstspingproject.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Security Audit Log Entity
 * Records security-related events for compliance and monitoring
 */
@Entity
@Table(name = "security_audit_logs")
@Data
@NoArgsConstructor
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String eventType; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_REFRESH, ACCESS_DENIED, etc.

    @Column
    private Long userId;

    @Column
    private String username;

    @Column(nullable = false)
    private String ipAddress;

    @Column
    private String userAgent;

    @Column(length = 500)
    private String details;

    @Column
    private String resourcePath;

    @Column
    private String httpMethod;

    @Column
    private Boolean successful;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SecurityAuditLog log = new SecurityAuditLog();

        public Builder eventType(String eventType) {
            log.setEventType(eventType);
            return this;
        }

        public Builder userId(Long userId) {
            log.setUserId(userId);
            return this;
        }

        public Builder username(String username) {
            log.setUsername(username);
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            log.setIpAddress(ipAddress);
            return this;
        }

        public Builder userAgent(String userAgent) {
            log.setUserAgent(userAgent);
            return this;
        }

        public Builder details(String details) {
            log.setDetails(details);
            return this;
        }

        public Builder resourcePath(String resourcePath) {
            log.setResourcePath(resourcePath);
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            log.setHttpMethod(httpMethod);
            return this;
        }

        public Builder successful(Boolean successful) {
            log.setSuccessful(successful);
            return this;
        }

        public SecurityAuditLog build() {
            log.setTimestamp(LocalDateTime.now());
            return log;
        }
    }
}
