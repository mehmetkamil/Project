package com.yeditepe.firstspingproject;

import com.yeditepe.firstspingproject.entity.Event;
import com.yeditepe.firstspingproject.entity.Ticket;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.EventRepository;
import com.yeditepe.firstspingproject.repository.TicketRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketService (Booking Microservice)
 * Tests ticket booking, cancellation, and retrieval operations
 */
@SuppressWarnings("null")
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket testTicket;
    private Event testEvent;
    private User testBuyer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setUsername("buyer");
        
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setCapacity(100);
        testEvent.setAvailableSeats(50);
        testEvent.setPrice(50.0);
        testEvent.setStartDateTime(LocalDateTime.now().plusDays(7));
        
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTicketNumber("TKT-001");
        testTicket.setEvent(testEvent);
        testTicket.setBuyer(testBuyer);
        testTicket.setPrice(50.0);
        testTicket.setStatus("ACTIVE");
        testTicket.setPurchaseDate(LocalDateTime.now());
    }

    @Test
    void testGetAllTickets_ReturnsTicketList() {
        // Given
        List<Ticket> tickets = Arrays.asList(testTicket);
        when(ticketRepository.findAll()).thenReturn(tickets);

        // When
        List<Ticket> result = ticketService.getAllTickets();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TKT-001", result.get(0).getTicketNumber());
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void testGetTicketById_ExistingTicket_ReturnsTicket() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));

        // When
        Optional<Ticket> result = ticketService.getTicketById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("TKT-001", result.get().getTicketNumber());
    }

    @Test
    void testGetTicketByNumber_ExistingTicket_ReturnsTicket() {
        // Given
        when(ticketRepository.findByTicketNumber("TKT-001")).thenReturn(Optional.of(testTicket));

        // When
        Optional<Ticket> result = ticketService.getTicketByNumber("TKT-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("TKT-001", result.get().getTicketNumber());
    }

    @Test
    void testGetUserTickets_ReturnsBuyerTickets() {
        // Given
        List<Ticket> tickets = Arrays.asList(testTicket);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testBuyer));
        when(ticketRepository.findByBuyerId(1L)).thenReturn(tickets);

        // When
        List<Ticket> result = ticketService.getUserTickets(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetEventTickets_ReturnsEventTickets() {
        // Given
        List<Ticket> tickets = Arrays.asList(testTicket);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(1L)).thenReturn(tickets);

        // When
        List<Ticket> result = ticketService.getEventTickets(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testCancelTicket_ActiveTicket_Success() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        Ticket result = ticketService.cancelTicket(1L);

        // Then
        assertEquals("CANCELLED", result.getStatus());
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    void testUseTicket_ActiveTicket_Success() {
        // Given
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(testTicket);

        // When
        Ticket result = ticketService.useTicket(1L);

        // Then
        assertEquals("USED", result.getStatus());
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    void testCountSoldTicketsForEvent_ReturnsCount() {
        // Given
        List<Ticket> mockTickets = Arrays.asList(testTicket, testTicket, testTicket, testTicket, testTicket, testTicket, testTicket, testTicket, testTicket, testTicket);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(1L)).thenReturn(mockTickets);

        // When
        int result = ticketService.countSoldTicketsForEvent(1L);

        // Then
        assertEquals(10, result);
    }

    @Test
    void testGetEventRevenue_CalculatesCorrectly() {
        // Given
        List<Ticket> tickets = Arrays.asList(testTicket, testTicket);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(1L)).thenReturn(tickets);

        // When
        double result = ticketService.getEventRevenue(1L);

        // Then
        assertEquals(100.0, result);
    }
}
