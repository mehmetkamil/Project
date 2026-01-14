package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.repository.*;
import com.yeditepe.firstspingproject.service.CustomMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Monitoring Controller
 * Provides detailed monitoring and metrics endpoints
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    @Autowired
    private CustomMetricsService customMetricsService;

    @Autowired
    private HealthEndpoint healthEndpoint;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final LocalDateTime startTime = LocalDateTime.now();

    /**
     * Public health check (basic)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> basicHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return ResponseEntity.ok(health);
    }

    /**
     * Detailed health check (requires authentication)
     */
    @GetMapping("/health/detailed")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ORGANIZER')")
    public ResponseEntity<?> detailedHealth() {
        return ResponseEntity.ok(healthEndpoint.health());
    }

    /**
     * Application metrics overview
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();

        // Custom metrics
        Map<String, Object> custom = new LinkedHashMap<>();
        custom.put("active_users", customMetricsService.getActiveUsersCount());
        custom.put("total_revenue_cents", customMetricsService.getTotalRevenue());
        metrics.put("custom", custom);

        // Database metrics
        Map<String, Object> database = new LinkedHashMap<>();
        database.put("users_count", userRepository.count());
        database.put("events_count", eventRepository.count());
        database.put("tickets_count", ticketRepository.count());
        database.put("payments_count", paymentRepository.count());
        metrics.put("database", database);

        // JVM metrics
        metrics.put("jvm", getJvmMetrics());

        // System metrics
        metrics.put("system", getSystemMetrics());

        metrics.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return ResponseEntity.ok(metrics);
    }

    /**
     * JVM metrics
     */
    @GetMapping("/metrics/jvm")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getJvmMetrics() {
        Map<String, Object> jvm = new LinkedHashMap<>();

        // Memory
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryMXBean.getNonHeapMemoryUsage();

        Map<String, Object> heap = new LinkedHashMap<>();
        heap.put("init_mb", heapMemory.getInit() / (1024 * 1024));
        heap.put("used_mb", heapMemory.getUsed() / (1024 * 1024));
        heap.put("committed_mb", heapMemory.getCommitted() / (1024 * 1024));
        heap.put("max_mb", heapMemory.getMax() / (1024 * 1024));
        heap.put("usage_percent", String.format("%.2f%%", (heapMemory.getUsed() * 100.0) / heapMemory.getMax()));
        jvm.put("heap", heap);

        Map<String, Object> nonHeap = new LinkedHashMap<>();
        nonHeap.put("init_mb", nonHeapMemory.getInit() / (1024 * 1024));
        nonHeap.put("used_mb", nonHeapMemory.getUsed() / (1024 * 1024));
        nonHeap.put("committed_mb", nonHeapMemory.getCommitted() / (1024 * 1024));
        jvm.put("non_heap", nonHeap);

        // Threads
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new LinkedHashMap<>();
        threads.put("total", threadMXBean.getThreadCount());
        threads.put("daemon", threadMXBean.getDaemonThreadCount());
        threads.put("peak", threadMXBean.getPeakThreadCount());
        threads.put("total_started", threadMXBean.getTotalStartedThreadCount());
        jvm.put("threads", threads);

        // GC
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        List<Map<String, Object>> gcList = new ArrayList<>();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            Map<String, Object> gc = new LinkedHashMap<>();
            gc.put("name", gcBean.getName());
            gc.put("collection_count", gcBean.getCollectionCount());
            gc.put("collection_time_ms", gcBean.getCollectionTime());
            gcList.add(gc);
        }
        jvm.put("garbage_collectors", gcList);

        // Runtime
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("name", runtimeMXBean.getName());
        runtime.put("vm_name", runtimeMXBean.getVmName());
        runtime.put("vm_version", runtimeMXBean.getVmVersion());
        runtime.put("uptime_ms", runtimeMXBean.getUptime());
        runtime.put("uptime_formatted", formatUptime(runtimeMXBean.getUptime()));
        jvm.put("runtime", runtime);

        return ResponseEntity.ok(jvm);
    }

    /**
     * System metrics
     */
    @GetMapping("/metrics/system")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        Map<String, Object> system = new LinkedHashMap<>();

        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        system.put("os_name", osMXBean.getName());
        system.put("os_version", osMXBean.getVersion());
        system.put("os_arch", osMXBean.getArch());
        system.put("available_processors", osMXBean.getAvailableProcessors());
        system.put("system_load_average", osMXBean.getSystemLoadAverage());

        // Java info
        system.put("java_version", System.getProperty("java.version"));
        system.put("java_vendor", System.getProperty("java.vendor"));
        system.put("java_home", System.getProperty("java.home"));

        // Application uptime
        Duration uptime = Duration.between(startTime, LocalDateTime.now());
        system.put("application_start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        system.put("application_uptime", formatDuration(uptime));

        return ResponseEntity.ok(system);
    }

    /**
     * Database statistics
     */
    @GetMapping("/metrics/database")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDatabaseMetrics() {
        Map<String, Object> database = new LinkedHashMap<>();

        // Entity counts
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("users", userRepository.count());
        counts.put("events", eventRepository.count());
        counts.put("tickets", ticketRepository.count());
        counts.put("payments", paymentRepository.count());
        database.put("entity_counts", counts);

        database.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return ResponseEntity.ok(database);
    }

    /**
     * Application info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        info.put("name", "EventPlanner API");
        info.put("version", "1.0.0");
        info.put("description", "Event Planning and Ticket Management System");
        info.put("environment", getEnvironment());

        Map<String, Object> build = new LinkedHashMap<>();
        build.put("java_version", System.getProperty("java.version"));
        build.put("spring_boot_version", "3.5.6");
        info.put("build", build);

        Map<String, Object> uptime = new LinkedHashMap<>();
        Duration duration = Duration.between(startTime, LocalDateTime.now());
        uptime.put("start_time", startTime.format(DateTimeFormatter.ISO_DATE_TIME));
        uptime.put("uptime", formatDuration(duration));
        info.put("uptime", uptime);

        return ResponseEntity.ok(info);
    }

    // Helper methods
    private String formatUptime(long uptimeMs) {
        long seconds = uptimeMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return String.format("%dd %dh %dm %ds", days, hours % 24, minutes % 60, seconds % 60);
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    private String getEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null ? profile : "development";
    }
}
