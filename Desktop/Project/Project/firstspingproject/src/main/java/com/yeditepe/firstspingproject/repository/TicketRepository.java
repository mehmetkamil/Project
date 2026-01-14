package com.yeditepe.firstspingproject.repository;

import com.yeditepe.firstspingproject.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Find tickets by event
    List<Ticket> findByEventId(Long eventId);

    // Find tickets by buyer
    List<Ticket> findByBuyerId(Long buyerId);

    // Find ticket by ticket number
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    // Find tickets by status
    List<Ticket> findByStatus(String status);

    // Custom query - find active tickets for an event
    @Query("SELECT t FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'ACTIVE'")
    List<Ticket> findActiveTicketsForEvent(@Param("eventId") Long eventId);

    // Custom query - count tickets sold for an event
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId AND t.status = 'ACTIVE'")
    Integer countSoldTicketsForEvent(@Param("eventId") Long eventId);
}
