package com.yeditepe.paymentservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String transactionId;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, REFUNDED

    @Column(nullable = false)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, WALLET

    private String notes;

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
    }

    public Payment(String transactionId, Long ticketId, Long userId, Double amount, String paymentMethod) {
        this.transactionId = transactionId;
        this.ticketId = ticketId;
        this.userId = userId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = LocalDateTime.now();
        this.status = "PENDING";
    }
}
