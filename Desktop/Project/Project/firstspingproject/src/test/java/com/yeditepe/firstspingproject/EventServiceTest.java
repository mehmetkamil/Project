package com.yeditepe.firstspingproject;

import com.yeditepe.firstspingproject.entity.Event;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.EventRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import com.yeditepe.firstspingproject.service.EventService;
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
 * Unit tests for EventService (Event Catalog Microservice)
 * Tests event creation, retrieval, and management operations
 */
@SuppressWarnings("null")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private User testOrganizer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testOrganizer = new User();
        testOrganizer.setId(1L);
        testOrganizer.setUsername("organizer");
        
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setDescription("Test Description");
        testEvent.setLocation("Test Location");
        testEvent.setCapacity(100);
        testEvent.setAvailableSeats(100);
        testEvent.setPrice(50.0);
        testEvent.setCategory("Conference");
        testEvent.setOrganizer(testOrganizer);
        testEvent.setStartDateTime(LocalDateTime.now().plusDays(7));
        testEvent.setEndDateTime(LocalDateTime.now().plusDays(8));
    }

    @Test
    void testGetAllEvents_ReturnsEventList() {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        // When
        List<Event> result = eventService.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getTitle());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void testGetEventById_ExistingEvent_ReturnsEvent() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // When
        Optional<Event> result = eventService.getEventById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getTitle());
    }

    @Test
    void testGetEventsByCategory_ReturnsFilteredEvents() {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findByCategory("Conference")).thenReturn(events);

        // When
        List<Event> result = eventService.getEventsByCategory("Conference");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Conference", result.get(0).getCategory());
    }

    @Test
    void testGetEventsByLocation_ReturnsFilteredEvents() {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findByLocation("Test Location")).thenReturn(events);

        // When
        List<Event> result = eventService.getEventsByLocation("Test Location");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Location", result.get(0).getLocation());
    }

    @Test
    void testBookTicket_AvailableSeats_Success() {
        // Given
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // When
        eventService.bookTicket(1L);

        // Then
        verify(eventRepository, times(1)).save(testEvent);
        assertEquals(99, testEvent.getAvailableSeats());
    }

    @Test
    void testBookTicket_NoAvailableSeats_ThrowsException() {
        // Given
        testEvent.setAvailableSeats(0);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        // When & Then
        assertThrows(RuntimeException.class, () -> eventService.bookTicket(1L));
    }

    @Test
    void testDeleteEvent_ExistingEvent_Success() {
        // Given
        when(eventRepository.existsById(1L)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> eventService.deleteEvent(1L));

        // Then
        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteEvent_NonExistingEvent_ThrowsException() {
        // Given
        when(eventRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(RuntimeException.class, () -> eventService.deleteEvent(999L));
    }
}
