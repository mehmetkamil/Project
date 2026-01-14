package com.yeditepe.eventservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String category;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Event(String title, String description, String location, LocalDateTime startDateTime,
                 LocalDateTime endDateTime, Integer capacity, Double price, String category, Long organizerId) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.capacity = capacity;
        this.availableSeats = capacity;
        this.price = price;
        this.category = category;
        this.organizerId = organizerId;
    }
}
