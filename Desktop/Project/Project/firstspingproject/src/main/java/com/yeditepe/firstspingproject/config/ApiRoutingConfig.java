package com.yeditepe.firstspingproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API Routing Configuration
 * Configures API versioning and routing patterns
 */
@Configuration
public class ApiRoutingConfig implements WebMvcConfigurer {

    /**
     * API Routes Documentation:
     * 
     * AUTH SERVICE (/api/auth/**)
     * - POST /api/auth/register - User registration
     * - POST /api/auth/login - User login
     * - POST /api/auth/refresh - Refresh JWT token
     * - POST /api/auth/logout - User logout
     * 
     * USER SERVICE (/api/users/**)
     * - GET /api/users - Get all users (Admin)
     * - GET /api/users/{id} - Get user by ID
     * - PUT /api/users/{id} - Update user
     * - DELETE /api/users/{id} - Delete user (Admin)
     * 
     * EVENT SERVICE (/api/v1/events/**)
     * - GET /api/v1/events - Get all events
     * - GET /api/v1/events/{id} - Get event by ID
     * - POST /api/v1/events/create - Create event (Organizer)
     * - PUT /api/v1/events/{id} - Update event (Organizer)
     * - DELETE /api/v1/events/{id} - Delete event (Organizer/Admin)
     * - GET /api/v1/events/category/{category} - Filter by category
     * - GET /api/v1/events/location/{location} - Filter by location
     * 
     * BOOKING SERVICE (/api/v1/tickets/**)
     * - GET /api/v1/tickets - Get all tickets
     * - GET /api/v1/tickets/{id} - Get ticket by ID
     * - POST /api/v1/tickets/book - Book a ticket
     * - PUT /api/v1/tickets/{id}/cancel - Cancel ticket
     * - PUT /api/v1/tickets/{id}/use - Use ticket (check-in)
     * - GET /api/v1/tickets/user/{userId} - Get user's tickets
     * 
     * PAYMENT SERVICE (/api/v1/payments/**)
     * - GET /api/v1/payments - Get all payments
     * - GET /api/v1/payments/{id} - Get payment by ID
     * - POST /api/v1/payments/create - Create payment
     * - PUT /api/v1/payments/{id}/complete - Complete payment
     * - PUT /api/v1/payments/{id}/refund - Refund payment
     * - GET /api/v1/payments/user/{userId} - Get user's payments
     * 
     * ADMIN SERVICE (/api/admin/**)
     * - GET /api/admin/dashboard - Admin dashboard stats
     * - GET /api/admin/metrics - System metrics
     * - GET /api/admin/logs - Activity logs
     * 
     * HEALTH CHECK (/api/health/**)
     * - GET /api/health - Health status
     * - GET /api/health/ready - Readiness check
     * - GET /api/health/live - Liveness check
     */

    // Spring 6.0+ no longer requires trailing slash configuration
    // Trailing slash matching is handled automatically
}
