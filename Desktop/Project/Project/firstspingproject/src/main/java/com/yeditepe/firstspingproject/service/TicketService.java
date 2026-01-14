package com.yeditepe.firstspingproject.service;

import com.yeditepe.firstspingproject.entity.Event;
import com.yeditepe.firstspingproject.entity.Ticket;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.entity.Payment;
import com.yeditepe.firstspingproject.repository.EventRepository;
import com.yeditepe.firstspingproject.repository.TicketRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("null")
@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private PaymentRepository paymentRepository;

    // Create - Book a ticket
    @Transactional
    public Ticket bookTicket(Long eventId, Long buyerId) {
        
        // Check if event exists
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }

        Event event = eventOpt.get();

        // Check if buyer exists
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        if (!buyerOpt.isPresent()) {
            throw new RuntimeException("User not found with id: " + buyerId);
        }

        User buyer = buyerOpt.get();

        // Check if event has available seats
        if (event.getAvailableSeats() <= 0) {
            throw new RuntimeException("No available seats for this event");
        }

        // Generate unique ticket number
        String ticketNumber = "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create ticket
        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setEvent(event);
        ticket.setBuyer(buyer);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setPrice(event.getPrice());
        ticket.setStatus("ACTIVE");

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Reduce available seats in event
        eventService.bookTicket(eventId);

        // Create payment record
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setTicket(savedTicket);
        payment.setUser(buyer);
        payment.setAmount(savedTicket.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("COMPLETED");
        payment.setPaymentMethod("ONLINE");
        payment.setNotes("Bilet satın alma - " + event.getTitle());
        paymentRepository.save(payment);

        return savedTicket;
    }

    // Read - Get all tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Read - Get ticket by ID
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    // Read - Get ticket by ticket number
    public Optional<Ticket> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    // Read - Get all tickets for a user
    public List<Ticket> getUserTickets(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return ticketRepository.findByBuyerId(userId);
    }

    // Read - Get all tickets for an event
    public List<Ticket> getEventTickets(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (!eventOpt.isPresent()) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
        return ticketRepository.findByEventId(eventId);
    }

    // Update - Cancel ticket
    public Ticket cancelTicket(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (!ticketOpt.isPresent()) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // Check if ticket is already cancelled
        if ("CANCELLED".equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket is already cancelled");
        }

        // Update ticket status
        ticket.setStatus("CANCELLED");
        Ticket updatedTicket = ticketRepository.save(ticket);

        // Increase available seats in event
        eventService.cancelTicket(ticket.getEvent().getId());

        return updatedTicket;
    }

    // Update - Use ticket (mark as USED)
    public Ticket useTicket(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (!ticketOpt.isPresent()) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // Check if ticket is active
        if (!"ACTIVE".equals(ticket.getStatus())) {
            throw new RuntimeException("Ticket is not active. Current status: " + ticket.getStatus());
        }

        // Update ticket status
        ticket.setStatus("USED");
        return ticketRepository.save(ticket);
    }

    // Delete - Delete ticket
    public void deleteTicket(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (!ticketOpt.isPresent()) {
            throw new RuntimeException("Ticket not found with id: " + ticketId);
        }

        Ticket ticket = ticketOpt.get();

        // If not cancelled, restore available seats
        if (!"CANCELLED".equals(ticket.getStatus())) {
            eventService.cancelTicket(ticket.getEvent().getId());
        }

        ticketRepository.deleteById(ticketId);
    }

    // Business logic - Count sold tickets for an event
    public int countSoldTicketsForEvent(Long eventId) {
        return ticketRepository.countSoldTicketsForEvent(eventId);
    }

    // Business logic - Check ticket validity
    public boolean isTicketValid(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        return ticketOpt.isPresent() && "ACTIVE".equals(ticketOpt.get().getStatus());
    }

    // Business logic - Get active tickets for user
    public List<Ticket> getUserActiveTickets(Long userId) {
        List<Ticket> userTickets = getUserTickets(userId);
        return userTickets.stream()
                .filter(ticket -> "ACTIVE".equals(ticket.getStatus()))
                .toList();
    }

    // Business logic - Get revenue for event
    public double getEventRevenue(Long eventId) {
        List<Ticket> eventTickets = getEventTickets(eventId);
        return eventTickets.stream()
                .filter(ticket -> "ACTIVE".equals(ticket.getStatus()) || "USED".equals(ticket.getStatus()))
                .mapToDouble(Ticket::getPrice)
                .sum();
    }
}
