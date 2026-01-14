package com.yeditepe.userservice.repository;

import com.yeditepe.userservice.entity.SecurityAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityAuditLogRepository extends JpaRepository<SecurityAuditLog, Long> {

    List<SecurityAuditLog> findByEventType(String eventType);
    List<SecurityAuditLog> findByUserId(Long userId);
    List<SecurityAuditLog> findByUsername(String username);
    List<SecurityAuditLog> findByIpAddress(String ipAddress);
    List<SecurityAuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    Page<SecurityAuditLog> findByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.eventType = :eventType AND sal.timestamp BETWEEN :start AND :end")
    List<SecurityAuditLog> findByEventTypeAndTimeRange(
            @Param("eventType") String eventType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.userId = :userId ORDER BY sal.timestamp DESC")
    Page<SecurityAuditLog> findRecentLogsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(sal) FROM SecurityAuditLog sal WHERE sal.eventType = :eventType AND sal.timestamp > :since")
    long countByEventTypeSince(@Param("eventType") String eventType, @Param("since") LocalDateTime since);

    @Query("SELECT sal.eventType, COUNT(sal) FROM SecurityAuditLog sal WHERE sal.timestamp BETWEEN :start AND :end GROUP BY sal.eventType")
    List<Object[]> getEventTypeStatistics(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT sal FROM SecurityAuditLog sal WHERE sal.successful = false AND sal.timestamp > :since")
    List<SecurityAuditLog> findRecentFailedAttempts(@Param("since") LocalDateTime since);
}
