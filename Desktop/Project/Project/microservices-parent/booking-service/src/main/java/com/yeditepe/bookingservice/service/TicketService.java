package com.yeditepe.bookingservice.service;

import com.yeditepe.bookingservice.client.EventClient;
import com.yeditepe.bookingservice.entity.Ticket;
import com.yeditepe.bookingservice.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventClient eventClient;

    @Transactional
    public Ticket bookTicket(Long eventId, Long buyerId) {
        
        // Check if event exists via event-service
        try {
            eventClient.getEventById(eventId);
        } catch (Exception e) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }

        // Generate unique ticket number
        String ticketNumber = "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create ticket - price will be fetched from event-service in a real implementation
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setEventId(eventId);
        ticket.setBuyerId(buyerId);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setPrice(0.0); // Price should be fetched from event
        ticket.setStatus("ACTIVE");

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Update event seats via event-service
        try {
            eventClient.bookTicket(eventId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update event seats: " + e.getMessage());
        }

        return savedTicket;
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public Optional<Ticket> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    public List<Ticket> getUserTickets(Long userId) {
        return ticketRepository.findByBuyerId(userId);
    }

    public List<Ticket> getEventTickets(Long eventId) {
        return ticketRepository.findByEventId(eventId);
    }

    public List<Ticket> getUserActiveTickets(Long userId) {
        List<Ticket> allTickets = ticketRepository.findByBuyerId(userId);
        return allTickets.stream()
                .filter(t -> "ACTIVE".equals(t.getStatus()))
                .toList();
    }

    @Transactional
    public Ticket cancelTicket(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (!ticketOpt.isPresent()) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        if ("CANCELLED".equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket is already cancelled");
        }

        ticket.setStatus("CANCELLED");
        Ticket updatedTicket = ticketRepository.save(ticket);

        // Update event seats via event-service
        try {
            eventClient.cancelTicket(ticket.getEventId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to update event seats: " + e.getMessage());
        }

        return updatedTicket;
    }

    public Ticket useTicket(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (!ticketOpt.isPresent()) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        if (!"ACTIVE".equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket cannot be used. Current status: " + ticket.getStatus());
        }

        ticket.setStatus("USED");
        return ticketRepository.save(ticket);
    }

    public void deleteTicket(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }
        ticketRepository.deleteById(ticketId);
    }

    public int countSoldTicketsForEvent(Long eventId) {
        return ticketRepository.countSoldTicketsForEvent(eventId);
    }

    public double getEventRevenue(Long eventId) {
        List<Ticket> tickets = ticketRepository.findActiveTicketsForEvent(eventId);
        return tickets.stream()
                .mapToDouble(Ticket::getPrice)
                .sum();
    }

    public boolean isTicketValid(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        return ticketOpt.isPresent() && "ACTIVE".equals(ticketOpt.get().getStatus());
    }
}
