package com.yeditepe.firstspingproject.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.sql.Connection;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides endpoints for monitoring application health and readiness
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.application.name:EventPlanner}")
    private String applicationName;

    private static final LocalDateTime startTime = LocalDateTime.now();

    /**
     * Basic health check
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        health.put("uptime", getUptime());
        
        return ResponseEntity.ok(health);
    }

    /**
     * Readiness check - checks if app is ready to serve traffic
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> status = new HashMap<>();
        Map<String, Object> checks = new HashMap<>();
        
        // Check database connectivity
        boolean dbHealthy = checkDatabaseHealth();
        checks.put("database", dbHealthy ? "UP" : "DOWN");
        
        // Overall status
        boolean ready = dbHealthy;
        status.put("status", ready ? "READY" : "NOT_READY");
        status.put("checks", checks);
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return ready ? ResponseEntity.ok(status) : ResponseEntity.status(503).body(status);
    }

    /**
     * Liveness check - checks if app is alive
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ALIVE");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return ResponseEntity.ok(status);
    }

    /**
     * Detailed health metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // JVM metrics
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("uptime_ms", runtimeMXBean.getUptime());
        jvm.put("uptime_formatted", getUptime());
        jvm.put("start_time", runtimeMXBean.getStartTime());
        
        Map<String, Object> memory = new HashMap<>();
        long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
        memory.put("heap_used_mb", heapUsed / (1024 * 1024));
        memory.put("heap_max_mb", heapMax / (1024 * 1024));
        memory.put("heap_usage_percent", (heapUsed * 100) / heapMax);
        
        Map<String, Object> system = new HashMap<>();
        system.put("available_processors", Runtime.getRuntime().availableProcessors());
        system.put("java_version", System.getProperty("java.version"));
        system.put("os_name", System.getProperty("os.name"));
        
        metrics.put("jvm", jvm);
        metrics.put("memory", memory);
        metrics.put("system", system);
        metrics.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Check database connectivity
     */
    private boolean checkDatabaseHealth() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate uptime
     */
    private String getUptime() {
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
