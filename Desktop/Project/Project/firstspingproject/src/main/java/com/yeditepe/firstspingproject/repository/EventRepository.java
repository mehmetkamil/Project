package com.yeditepe.firstspingproject.repository;

import com.yeditepe.firstspingproject.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Find events by category
    List<Event> findByCategory(String category);

    // Find events by location
    List<Event> findByLocation(String location);

    // Find events by organizer
    List<Event> findByOrganizerId(Long organizerId);

    // Custom query - find events with available seats
    @Query("SELECT e FROM Event e WHERE e.availableSeats > 0")
    List<Event> findEventsWithAvailableSeats();

    // Custom query - find events by title
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> searchByTitle(@Param("title") String title);
}
