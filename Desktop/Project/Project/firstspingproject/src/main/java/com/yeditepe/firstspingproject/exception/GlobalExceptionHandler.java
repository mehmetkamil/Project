package com.yeditepe.firstspingproject.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * Handles all exceptions across the application and returns consistent error responses
 */
@SuppressWarnings("null")
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    /**
     * Handle resource not found exceptions
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFoundException(
            NoHandlerFoundException ex, WebRequest request) {
        
        logger.warn("Resource not found: {}", ex.getRequestURL());
        
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                "The requested resource was not found: " + ex.getRequestURL(),
                request.getDescription(false)
        );
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Access denied: {}", ex.getMessage());
        
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You don't have permission to access this resource",
                request.getDescription(false)
        );
    }

    /**
     * Handle bad credentials exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        logger.warn("Bad credentials: {}", ex.getMessage());
        
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                "Invalid username or password",
                request.getDescription(false)
        );
    }

    /**
     * Handle validation exceptions
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
        );
        
        logger.warn("Validation failed: {}", errors);
        
        Map<String, Object> response = buildErrorResponseBody(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                "Request validation failed",
                request.getDescription(false)
        );
        response.put("validationErrors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false)
        );
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(
            Exception ex, WebRequest request) {
        
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false)
        );
    }

    /**
     * Build error response
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message, String path) {
        
        return new ResponseEntity<>(
                buildErrorResponseBody(status, error, message, path),
                status
        );
    }

    /**
     * Build error response body
     */
    private Map<String, Object> buildErrorResponseBody(
            HttpStatus status, String error, String message, String path) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        response.put("path", path.replace("uri=", ""));
        
        return response;
    }
}
