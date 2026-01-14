package com.yeditepe.firstspingproject.filter;

import com.yeditepe.firstspingproject.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting Filter
 * Applies rate limiting to all API requests based on client IP address
 */
@SuppressWarnings("null")
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting for static resources
        String path = request.getRequestURI();
        if (isStaticResource(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get client IP address
        String clientIp = getClientIP(request);
        
        // Check if user is authenticated (premium rate limit)
        boolean isAuthenticated = request.getHeader("Authorization") != null;
        
        // Get appropriate bucket
        Bucket bucket = isAuthenticated 
                ? rateLimitConfig.resolvePremiumBucket(clientIp)
                : rateLimitConfig.resolveBucket(clientIp);

        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Limit", isAuthenticated ? "500" : "100");
            
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitTimeSeconds));
            response.addHeader("X-Rate-Limit-Remaining", "0");
            
            String jsonResponse = String.format(
                "{\"error\": \"Rate limit exceeded\", \"message\": \"Too many requests. Please try again in %d seconds.\", \"retryAfter\": %d}",
                waitTimeSeconds, waitTimeSeconds
            );
            response.getWriter().write(jsonResponse);
        }
    }

    /**
     * Get client IP address, considering proxy headers
     */
    private String getClientIP(HttpServletRequest request) {
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
