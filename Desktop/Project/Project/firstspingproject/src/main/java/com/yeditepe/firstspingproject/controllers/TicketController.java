package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.entity.Ticket;
import com.yeditepe.firstspingproject.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // Welcome endpoint
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Booking Service!";
    }

    // Create - Book a ticket
    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(
            @RequestParam Long eventId,
            @RequestParam Long buyerId) {
        try {
            Ticket ticket = ticketService.bookTicket(eventId, buyerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Read - Get all tickets
    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    // Read - Get ticket by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable Long id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        if (ticket.isPresent()) {
            return ResponseEntity.ok(ticket.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found with id: " + id);
        }
    }

    // Read - Get ticket by ticket number
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<?> getTicketByNumber(@PathVariable String ticketNumber) {
        Optional<Ticket> ticket = ticketService.getTicketByNumber(ticketNumber);
        if (ticket.isPresent()) {
            return ResponseEntity.ok(ticket.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found with number: " + ticketNumber);
        }
    }

    // Read - Get all tickets for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserTickets(@PathVariable Long userId) {
        try {
            List<Ticket> tickets = ticketService.getUserTickets(userId);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Read - Get all tickets for an event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventTickets(@PathVariable Long eventId) {
        try {
            List<Ticket> tickets = ticketService.getEventTickets(eventId);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Read - Get active tickets for a user
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<?> getUserActiveTickets(@PathVariable Long userId) {
        try {
            List<Ticket> tickets = ticketService.getUserActiveTickets(userId);
            return ResponseEntity.ok(tickets);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Update - Cancel ticket
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id) {
        try {
            Ticket cancelledTicket = ticketService.cancelTicket(id);
            return ResponseEntity.ok(cancelledTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Update - Use ticket (check in)
    @PutMapping("/{id}/use")
    public ResponseEntity<?> useTicket(@PathVariable Long id) {
        try {
            Ticket usedTicket = ticketService.useTicket(id);
            return ResponseEntity.ok(usedTicket);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete - Delete ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long id) {
        try {
            ticketService.deleteTicket(id);
            return ResponseEntity.ok("Ticket deleted successfully with id: " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Business logic - Count sold tickets for event
    @GetMapping("/event/{eventId}/sold-count")
    public ResponseEntity<?> getSoldTicketsCount(@PathVariable Long eventId) {
        try {
            int count = ticketService.countSoldTicketsForEvent(eventId);
            return ResponseEntity.ok("Sold tickets for event " + eventId + ": " + count);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Business logic - Get event revenue
    @GetMapping("/event/{eventId}/revenue")
    public ResponseEntity<?> getEventRevenue(@PathVariable Long eventId) {
        try {
            double revenue = ticketService.getEventRevenue(eventId);
            return ResponseEntity.ok("Revenue for event " + eventId + ": " + revenue);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Business logic - Check ticket validity
    @GetMapping("/{id}/valid")
    public ResponseEntity<?> checkTicketValidity(@PathVariable Long id) {
        boolean isValid = ticketService.isTicketValid(id);
        if (isValid) {
            return ResponseEntity.ok("Ticket is valid and can be used");
        } else {
            return ResponseEntity.ok("Ticket is not valid");
        }
    }
}
