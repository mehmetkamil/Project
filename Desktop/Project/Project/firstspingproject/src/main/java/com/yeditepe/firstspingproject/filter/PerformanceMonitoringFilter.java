package com.yeditepe.firstspingproject.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Performance Monitoring Filter
 * Tracks request processing time and adds correlation IDs
 */
@Component
@Order(1)
public class PerformanceMonitoringFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitoringFilter.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE");

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate or extract request ID
        String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Generate or extract correlation ID
        String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add to MDC for logging context
        MDC.put("requestId", requestId);
        MDC.put("correlationId", correlationId);
        MDC.put("clientIp", getClientIp(httpRequest));
        MDC.put("method", httpRequest.getMethod());
        MDC.put("uri", httpRequest.getRequestURI());

        // Add headers to response
        httpResponse.setHeader(REQUEST_ID_HEADER, requestId);
        httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log performance metrics
            String performanceLog = String.format(
                    "requestId=%s | method=%s | uri=%s | status=%d | duration=%dms | ip=%s",
                    requestId,
                    httpRequest.getMethod(),
                    httpRequest.getRequestURI(),
                    httpResponse.getStatus(),
                    duration,
                    getClientIp(httpRequest)
            );
            
            performanceLogger.info(performanceLog);

            // Log slow requests (> 1 second)
            if (duration > 1000) {
                logger.warn("SLOW REQUEST: {} {} took {}ms", 
                        httpRequest.getMethod(), 
                        httpRequest.getRequestURI(), 
                        duration);
            }

            // Log very slow requests (> 5 seconds)
            if (duration > 5000) {
                logger.error("VERY SLOW REQUEST: {} {} took {}ms - Investigate immediately!", 
                        httpRequest.getMethod(), 
                        httpRequest.getRequestURI(), 
                        duration);
            }

            // Clear MDC
            MDC.clear();
        }
    }

    private String getClientIp(HttpServletRequest request) {
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
