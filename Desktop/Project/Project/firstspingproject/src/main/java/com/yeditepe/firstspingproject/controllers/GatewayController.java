package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.config.RateLimitConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API Gateway Controller
 * Provides information about available API routes and gateway status
 */
@RestController
@RequestMapping("/api/gateway")
public class GatewayController {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    /**
     * Get API Gateway information and available routes
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("name", "EventPlanner API Gateway");
        info.put("version", "1.0.0");
        info.put("status", "RUNNING");
        
        // Rate limit info
        Map<String, Object> rateLimit = new HashMap<>();
        rateLimit.put("standard_limit", "100 requests/minute");
        rateLimit.put("premium_limit", "500 requests/minute (authenticated)");
        rateLimit.put("tracked_ips", rateLimitConfig.getTrackedIpCount());
        info.put("rate_limiting", rateLimit);
        
        return ResponseEntity.ok(info);
    }

    /**
     * Get all available API routes
     */
    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        Map<String, Object> routes = new LinkedHashMap<>();
        
        // Auth routes
        Map<String, String> authRoutes = new LinkedHashMap<>();
        authRoutes.put("POST /api/auth/register", "Register new user");
        authRoutes.put("POST /api/auth/login", "Login user");
        routes.put("Auth Service", authRoutes);
        
        // User routes
        Map<String, String> userRoutes = new LinkedHashMap<>();
        userRoutes.put("GET /api/users", "Get all users");
        userRoutes.put("GET /api/users/{id}", "Get user by ID");
        userRoutes.put("PUT /api/users/{id}", "Update user");
        userRoutes.put("DELETE /api/users/{id}", "Delete user");
        routes.put("User Service", userRoutes);
        
        // Event routes
        Map<String, String> eventRoutes = new LinkedHashMap<>();
        eventRoutes.put("GET /api/v1/events", "Get all events");
        eventRoutes.put("GET /api/v1/events/{id}", "Get event by ID");
        eventRoutes.put("POST /api/v1/events/create", "Create new event");
        eventRoutes.put("PUT /api/v1/events/{id}", "Update event");
        eventRoutes.put("DELETE /api/v1/events/{id}", "Delete event");
        eventRoutes.put("GET /api/v1/events/category/{category}", "Filter by category");
        eventRoutes.put("GET /api/v1/events/location/{location}", "Filter by location");
        eventRoutes.put("GET /api/v1/events/organizer/{organizerId}", "Get organizer's events");
        routes.put("Event Service", eventRoutes);
        
        // Ticket routes
        Map<String, String> ticketRoutes = new LinkedHashMap<>();
        ticketRoutes.put("GET /api/v1/tickets", "Get all tickets");
        ticketRoutes.put("GET /api/v1/tickets/{id}", "Get ticket by ID");
        ticketRoutes.put("POST /api/v1/tickets/book", "Book a ticket");
        ticketRoutes.put("PUT /api/v1/tickets/{id}/cancel", "Cancel ticket");
        ticketRoutes.put("PUT /api/v1/tickets/{id}/use", "Use ticket (check-in)");
        ticketRoutes.put("GET /api/v1/tickets/user/{userId}", "Get user's tickets");
        ticketRoutes.put("GET /api/v1/tickets/event/{eventId}", "Get event's tickets");
        routes.put("Booking Service", ticketRoutes);
        
        // Payment routes
        Map<String, String> paymentRoutes = new LinkedHashMap<>();
        paymentRoutes.put("GET /api/v1/payments", "Get all payments");
        paymentRoutes.put("GET /api/v1/payments/{id}", "Get payment by ID");
        paymentRoutes.put("POST /api/v1/payments/create", "Create payment");
        paymentRoutes.put("PUT /api/v1/payments/{id}/complete", "Complete payment");
        paymentRoutes.put("PUT /api/v1/payments/{id}/cancel", "Cancel payment");
        paymentRoutes.put("PUT /api/v1/payments/{id}/refund", "Refund payment");
        paymentRoutes.put("GET /api/v1/payments/user/{userId}", "Get user's payments");
        routes.put("Payment Service", paymentRoutes);
        
        // Health routes
        Map<String, String> healthRoutes = new LinkedHashMap<>();
        healthRoutes.put("GET /api/health", "Health check");
        healthRoutes.put("GET /api/health/ready", "Readiness check");
        healthRoutes.put("GET /api/health/live", "Liveness check");
        healthRoutes.put("GET /api/health/metrics", "System metrics");
        routes.put("Health Service", healthRoutes);
        
        // Gateway routes
        Map<String, String> gatewayRoutes = new LinkedHashMap<>();
        gatewayRoutes.put("GET /api/gateway", "Gateway info");
        gatewayRoutes.put("GET /api/gateway/routes", "Available routes");
        gatewayRoutes.put("GET /api/gateway/stats", "Gateway statistics");
        routes.put("Gateway Service", gatewayRoutes);
        
        return ResponseEntity.ok(routes);
    }

    /**
     * Get gateway statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Rate limit statistics
        Map<String, Object> rateLimitStats = new HashMap<>();
        rateLimitStats.put("tracked_ips", rateLimitConfig.getTrackedIpCount());
        rateLimitStats.put("bucket_stats", rateLimitConfig.getBucketStats());
        stats.put("rate_limiting", rateLimitStats);
        
        // JVM stats
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvmStats = new HashMap<>();
        jvmStats.put("total_memory_mb", runtime.totalMemory() / (1024 * 1024));
        jvmStats.put("free_memory_mb", runtime.freeMemory() / (1024 * 1024));
        jvmStats.put("used_memory_mb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        jvmStats.put("max_memory_mb", runtime.maxMemory() / (1024 * 1024));
        stats.put("jvm", jvmStats);
        
        return ResponseEntity.ok(stats);
    }
}
