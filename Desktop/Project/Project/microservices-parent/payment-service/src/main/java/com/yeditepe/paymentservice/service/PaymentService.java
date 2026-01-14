package com.yeditepe.paymentservice.service;

import com.yeditepe.paymentservice.client.TicketClient;
import com.yeditepe.paymentservice.entity.Payment;
import com.yeditepe.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketClient ticketClient;

    public Payment createPayment(Long ticketId, Long userId, String paymentMethod) {
        // Verify ticket exists via booking-service
        try {
            ResponseEntity<?> ticketResponse = ticketClient.getTicketById(ticketId);
            if (!ticketResponse.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Ticket not found with id: " + ticketId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify ticket with booking-service: " + e.getMessage());
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setTicketId(ticketId);
        payment.setUserId(userId);
        payment.setAmount(0.0); // Amount should be fetched from ticket
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PENDING");
        
        return paymentRepository.save(payment);
    }

    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (!"PENDING".equals(payment.getStatus())) {
            throw new RuntimeException("Payment cannot be completed. Current status: " + payment.getStatus());
        }
        
        payment.setStatus("COMPLETED");
        return paymentRepository.save(payment);
    }

    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if ("COMPLETED".equals(payment.getStatus()) || "REFUNDED".equals(payment.getStatus())) {
            throw new RuntimeException("Payment cannot be cancelled. Current status: " + payment.getStatus());
        }
        
        payment.setStatus("FAILED");
        return paymentRepository.save(payment);
    }

    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Only completed payments can be refunded");
        }
        
        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found with transaction ID: " + transactionId));
    }

    public String getPaymentStatus(Long paymentId) {
        Payment payment = getPaymentById(paymentId);
        return payment.getStatus();
    }

    public Double getUserSpending(Long userId) {
        Double spending = paymentRepository.getTotalSpendingByUser(userId);
        return spending != null ? spending : 0.0;
    }

    public Double getTotalRevenue() {
        Double revenue = paymentRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }
}
