package com.yeditepe.bookingservice.repository;

import com.yeditepe.bookingservice.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventId(Long eventId);

    List<Ticket> findByBuyerId(Long buyerId);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByStatus(String status);

    @Query("SELECT t FROM Ticket t WHERE t.eventId = :eventId AND t.status = 'ACTIVE'")
    List<Ticket> findActiveTicketsForEvent(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.eventId = :eventId AND t.status = 'ACTIVE'")
    Integer countSoldTicketsForEvent(@Param("eventId") Long eventId);
}
