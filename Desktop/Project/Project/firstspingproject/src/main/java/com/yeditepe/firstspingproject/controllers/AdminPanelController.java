package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.entity.*;
import com.yeditepe.firstspingproject.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("null")
@RestController
@RequestMapping("/api/admin-panel")
@CrossOrigin(origins = "*")
public class AdminPanelController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SecurityAuditLogRepository securityAuditLogRepository;

    /**
     * Get all users with their roles
     */
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> userDTOs = users.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("userName", user.getUsername());
            userMap.put("email", user.getEmail());
            userMap.put("roles", user.getRoles().stream()
                .map(role -> {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put("name", role.getName());
                    return roleMap;
                })
                .collect(Collectors.toList()));
            return userMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("userName", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("roles", user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toList()));
                return ResponseEntity.ok(userMap);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all events
     */
    @GetMapping("/events/all")
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return ResponseEntity.ok(events);
    }

    /**
     * Get event by ID
     */
    @GetMapping("/events/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all tickets
     */
    @GetMapping("/tickets")
    public ResponseEntity<List<Map<String, Object>>> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        
        List<Map<String, Object>> ticketDTOs = tickets.stream().map(ticket -> {
            Map<String, Object> ticketMap = new HashMap<>();
            ticketMap.put("id", ticket.getId());
            ticketMap.put("ticketNumber", ticket.getTicketNumber());
            ticketMap.put("price", ticket.getPrice());
            ticketMap.put("purchaseDate", ticket.getPurchaseDate());
            ticketMap.put("status", ticket.getStatus());
            
            if (ticket.getEvent() != null) {
                ticketMap.put("eventId", ticket.getEvent().getId());
                ticketMap.put("eventTitle", ticket.getEvent().getTitle());
            }
            
            if (ticket.getBuyer() != null) {
                ticketMap.put("buyerId", ticket.getBuyer().getId());
                ticketMap.put("buyerName", ticket.getBuyer().getUsername());
            }
            
            return ticketMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(ticketDTOs);
    }

    /**
     * Get all payments
     */
    @GetMapping("/payments")
    public ResponseEntity<List<Map<String, Object>>> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        
        List<Map<String, Object>> paymentDTOs = payments.stream().map(payment -> {
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", payment.getId());
            paymentMap.put("transactionId", payment.getTransactionId());
            paymentMap.put("amount", payment.getAmount());
            paymentMap.put("paymentMethod", payment.getPaymentMethod());
            paymentMap.put("paymentDate", payment.getPaymentDate());
            paymentMap.put("status", payment.getStatus());
            paymentMap.put("notes", payment.getNotes());
            
            if (payment.getUser() != null) {
                paymentMap.put("userId", payment.getUser().getId());
                paymentMap.put("userName", payment.getUser().getUsername());
            }
            
            if (payment.getTicket() != null) {
                paymentMap.put("ticketId", payment.getTicket().getId());
                paymentMap.put("ticketNumber", payment.getTicket().getTicketNumber());
            }
            
            return paymentMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(paymentDTOs);
    }

    /**
     * Get security audit logs
     */
    @GetMapping("/security/logs")
    public ResponseEntity<List<Map<String, Object>>> getSecurityLogs(
            @RequestParam(required = false, defaultValue = "100") int limit) {
        List<SecurityAuditLog> logs = securityAuditLogRepository
            .findByOrderByTimestampDesc(PageRequest.of(0, Math.min(limit, 500)))
            .getContent();
        
        List<Map<String, Object>> logDTOs = logs.stream().map(log -> {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("id", log.getId());
            logMap.put("timestamp", log.getTimestamp());
            logMap.put("eventType", log.getEventType());
            logMap.put("userId", log.getUserId());
            logMap.put("username", log.getUsername());
            logMap.put("ipAddress", log.getIpAddress());
            logMap.put("userAgent", log.getUserAgent());
            logMap.put("details", log.getDetails());
            logMap.put("resourcePath", log.getResourcePath());
            logMap.put("httpMethod", log.getHttpMethod());
            logMap.put("successful", log.getSuccessful());
            return logMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(logDTOs);
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // User statistics
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        // Event statistics
        long totalEvents = eventRepository.count();
        stats.put("totalEvents", totalEvents);
        
        // Ticket statistics
        long totalTickets = ticketRepository.count();
        stats.put("totalTickets", totalTickets);
        
        // Payment statistics
        List<Payment> payments = paymentRepository.findAll();
        double totalRevenue = payments.stream()
            .mapToDouble(Payment::getAmount)
            .sum();
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalPayments", payments.size());
        
        // Security statistics
        long totalSecurityLogs = securityAuditLogRepository.count();
        long failedLogins = securityAuditLogRepository.findByEventType("LOGIN_FAILURE").size();
        stats.put("totalSecurityLogs", totalSecurityLogs);
        stats.put("failedLogins", failedLogins);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Delete user (soft delete or hard delete)
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        userRepository.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete event
     */
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        if (!eventRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        eventRepository.deleteById(id);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Event deleted successfully");
        return ResponseEntity.ok(response);
    }
}
