package com.yeditepe.eventservice.service;

import com.yeditepe.eventservice.entity.Event;
import com.yeditepe.eventservice.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event createEvent(String title, String description, String location, 
                             LocalDateTime startDateTime, LocalDateTime endDateTime, 
                             int capacity, double price, String category, Long organizerId) {
        
        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setStartDateTime(startDateTime);
        event.setEndDateTime(endDateTime);
        event.setCapacity(capacity);
        event.setAvailableSeats(capacity);
        event.setPrice(price);
        event.setCategory(category);
        event.setOrganizerId(organizerId);

        return eventRepository.save(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findByCategory(category);
    }

    public List<Event> getEventsByLocation(String location) {
        return eventRepository.findByLocation(location);
    }

    public List<Event> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    public Event updateEvent(Long id, String title, String description, String location,
                           LocalDateTime startDateTime, LocalDateTime endDateTime,
                           int capacity, double price, String category) {
        
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (!eventOpt.isPresent()) {
            throw new RuntimeException("Event not found with id: " + id);
        }

        Event event = eventOpt.get();
        
        if (title != null) event.setTitle(title);
        if (description != null) event.setDescription(description);
        if (location != null) event.setLocation(location);
        if (startDateTime != null) event.setStartDateTime(startDateTime);
        if (endDateTime != null) event.setEndDateTime(endDateTime);
        if (capacity > 0) {
            event.setCapacity(capacity);
            int soldTickets = event.getCapacity() - event.getAvailableSeats();
            event.setAvailableSeats(capacity - soldTickets);
        }
        if (price >= 0) event.setPrice(price);
        if (category != null) event.setCategory(category);

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    public boolean hasAvailableSeats(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        return eventOpt.isPresent() && eventOpt.get().getAvailableSeats() > 0;
    }

    public void bookTicket(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            if (event.getAvailableSeats() > 0) {
                event.setAvailableSeats(event.getAvailableSeats() - 1);
                eventRepository.save(event);
            } else {
                throw new RuntimeException("No available seats for event id: " + eventId);
            }
        } else {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
    }

    public void cancelTicket(Long eventId) {
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            if (event.getAvailableSeats() < event.getCapacity()) {
                event.setAvailableSeats(event.getAvailableSeats() + 1);
                eventRepository.save(event);
            } else {
                throw new RuntimeException("Cannot cancel - event already at full capacity");
            }
        } else {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
    }
}
