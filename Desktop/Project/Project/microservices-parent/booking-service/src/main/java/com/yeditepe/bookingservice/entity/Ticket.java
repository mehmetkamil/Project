package com.yeditepe.bookingservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String status; // ACTIVE, USED, CANCELLED

    @PrePersist
    protected void onCreate() {
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }

    public Ticket(String ticketNumber, Long eventId, Long buyerId, Double price) {
        this.ticketNumber = ticketNumber;
        this.eventId = eventId;
        this.buyerId = buyerId;
        this.price = price;
        this.purchaseDate = LocalDateTime.now();
        this.status = "ACTIVE";
    }
}
