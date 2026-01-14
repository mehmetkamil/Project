package com.yeditepe.firstspingproject.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Request/Response Logging Filter
 * Logs all API requests for monitoring and debugging
 */
@SuppressWarnings("null")
@Component
@Order(2)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip logging for static resources
        String path = request.getRequestURI();
        if (isStaticResource(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate unique request ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        // Wrap request and response for caching
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Record start time
        long startTime = System.currentTimeMillis();
        String timestamp = LocalDateTime.now().format(formatter);

        // Add request ID to response headers
        wrappedResponse.addHeader("X-Request-ID", requestId);

        // Log incoming request
        logRequest(requestId, timestamp, wrappedRequest);

        try {
            // Process the request
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            // Calculate duration
            long duration = System.currentTimeMillis() - startTime;
            
            // Log response
            logResponse(requestId, wrappedResponse, duration);
            
            // Copy response body back to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Log incoming request details
     */
    private void logRequest(String requestId, String timestamp, ContentCachingRequestWrapper request) {
        String clientIp = getClientIP(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String userAgent = request.getHeader("User-Agent");
        String authorization = request.getHeader("Authorization") != null ? "Bearer ***" : "None";

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n╔══════════════════════════════════════════════════════════════╗\n");
        logMessage.append(String.format("║ REQUEST [%s] - %s\n", requestId, timestamp));
        logMessage.append("╠══════════════════════════════════════════════════════════════╣\n");
        logMessage.append(String.format("║ Method: %s\n", method));
        logMessage.append(String.format("║ URI: %s\n", uri));
        if (queryString != null) {
            logMessage.append(String.format("║ Query: %s\n", queryString));
        }
        logMessage.append(String.format("║ Client IP: %s\n", clientIp));
        logMessage.append(String.format("║ User-Agent: %s\n", userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "Unknown"));
        logMessage.append(String.format("║ Auth: %s\n", authorization));
        logMessage.append("╚══════════════════════════════════════════════════════════════╝");

        logger.info(logMessage.toString());
    }

    /**
     * Log response details
     */
    private void logResponse(String requestId, ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String statusText = getStatusText(status);

        StringBuilder logMessage = new StringBuilder();
        logMessage.append("\n╔══════════════════════════════════════════════════════════════╗\n");
        logMessage.append(String.format("║ RESPONSE [%s]\n", requestId));
        logMessage.append("╠══════════════════════════════════════════════════════════════╣\n");
        logMessage.append(String.format("║ Status: %d %s\n", status, statusText));
        logMessage.append(String.format("║ Duration: %d ms\n", duration));
        logMessage.append(String.format("║ Content-Type: %s\n", response.getContentType()));
        logMessage.append("╚══════════════════════════════════════════════════════════════╝");

        if (status >= 400) {
            logger.warn(logMessage.toString());
        } else {
            logger.info(logMessage.toString());
        }
    }

    /**
     * Get client IP address
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Get HTTP status text
     */
    private String getStatusText(int status) {
        return switch (status) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 429 -> "Too Many Requests";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }

    /**
     * Check if the request is for a static resource
     */
    private boolean isStaticResource(String path) {
        return path.startsWith("/css/") || 
               path.startsWith("/js/") || 
               path.startsWith("/images/") ||
               path.startsWith("/favicon") ||
               path.equals("/") ||
               path.equals("/index.html") ||
               path.startsWith("/h2-console");
    }
}
