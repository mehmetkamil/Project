package com.yeditepe.eventservice.repository;

import com.yeditepe.eventservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCategory(String category);

    List<Event> findByLocation(String location);

    List<Event> findByOrganizerId(Long organizerId);

    @Query("SELECT e FROM Event e WHERE e.availableSeats > 0")
    List<Event> findEventsWithAvailableSeats();

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Event> searchByTitle(@Param("title") String title);
}
