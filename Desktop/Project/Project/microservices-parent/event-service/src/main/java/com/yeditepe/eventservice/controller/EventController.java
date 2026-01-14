package com.yeditepe.eventservice.controller;

import com.yeditepe.eventservice.entity.Event;
import com.yeditepe.eventservice.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/events")
@CrossOrigin(origins = "*")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Event Catalog Service!";
    }

    @PostMapping("/create")
    public ResponseEntity<?> createEvent(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestParam int capacity,
            @RequestParam double price,
            @RequestParam String category,
            @RequestParam Long organizerId) {
        try {
            Event event = eventService.createEvent(title, description, location, startDateTime, 
                                                   endDateTime, capacity, price, category, organizerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        List<Event> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);
        if (event.isPresent()) {
            return ResponseEntity.ok(event.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found with id: " + id);
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable String category) {
        List<Event> events = eventService.getEventsByCategory(category);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Event>> getEventsByLocation(@PathVariable String location) {
        List<Event> events = eventService.getEventsByLocation(location);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getEventsByOrganizer(@PathVariable Long organizerId) {
        try {
            List<Event> events = eventService.getEventsByOrganizer(organizerId);
            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Double price,
            @RequestParam(required = false) String category) {
        try {
            Event updatedEvent = eventService.updateEvent(id, title, description, location, 
                                                         startDateTime, endDateTime, 
                                                         capacity != null ? capacity : 0, 
                                                         price != null ? price : -1, 
                                                         category);
            return ResponseEntity.ok(updatedEvent);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id) {
        try {
            eventService.deleteEvent(id);
            return ResponseEntity.ok("Event deleted successfully with id: " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/available-seats")
    public ResponseEntity<?> checkAvailableSeats(@PathVariable Long id) {
        Optional<Event> event = eventService.getEventById(id);
        if (event.isPresent()) {
            return ResponseEntity.ok("Available seats: " + event.get().getAvailableSeats() + 
                                    " / " + event.get().getCapacity());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Event not found with id: " + id);
        }
    }

    @PostMapping("/{id}/book-ticket")
    public ResponseEntity<?> bookTicket(@PathVariable Long id) {
        try {
            eventService.bookTicket(id);
            Optional<Event> event = eventService.getEventById(id);
            return ResponseEntity.ok("Ticket booked successfully. Remaining seats: " + 
                                    event.get().getAvailableSeats());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel-ticket")
    public ResponseEntity<?> cancelTicket(@PathVariable Long id) {
        try {
            eventService.cancelTicket(id);
            Optional<Event> event = eventService.getEventById(id);
            return ResponseEntity.ok("Ticket cancelled successfully. Available seats: " + 
                                    event.get().getAvailableSeats());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
