package com.yeditepe.firstspingproject.repository;

import com.yeditepe.firstspingproject.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Security Audit Logs
 */
@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    /**
     * Find logs by event type
     */
    List<SecurityAuditLog> findByEventType(String eventType);

    /**
     * Find logs by user ID
     */
    List<SecurityAuditLog> findByUserId(Long userId);

    /**
     * Find logs by username
     */
    List<SecurityAuditLog> findByUsername(String username);

    /**
     * Find logs by IP address
     */
    List<SecurityAuditLog> findByIpAddress(String ipAddress);

    /**
     * Find logs within a time range
     */
    List<SecurityAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find recent logs (paginated)
     */
    Page<SecurityAuditLog> findByOrderByTimestampDesc(Pageable pageable);

    /**
     * Find failed login attempts for a username
     */
    @Query("SELECT s FROM SecurityAuditLog s WHERE s.username = :username AND s.eventType = 'LOGIN_FAILURE' AND s.timestamp > :since ORDER BY s.timestamp DESC")
    List<SecurityAuditLog> findFailedLoginAttempts(@Param("username") String username, @Param("since") LocalDateTime since);

    /**
     * Count login failures by IP in time range
     */
    @Query("SELECT COUNT(s) FROM SecurityAuditLog s WHERE s.ipAddress = :ip AND s.eventType = 'LOGIN_FAILURE' AND s.timestamp > :since")
    long countLoginFailuresByIp(@Param("ip") String ip, @Param("since") LocalDateTime since);

    /**
     * Find suspicious activities (multiple failures)
     */
    @Query("SELECT s.ipAddress, COUNT(s) as failCount FROM SecurityAuditLog s WHERE s.eventType = 'LOGIN_FAILURE' AND s.timestamp > :since GROUP BY s.ipAddress HAVING COUNT(s) > :threshold")
    List<Object[]> findSuspiciousIps(@Param("since") LocalDateTime since, @Param("threshold") long threshold);

    /**
     * Delete old audit logs
     */
    void deleteByTimestampBefore(LocalDateTime before);
}
