package com.yeditepe.firstspingproject.service;

import com.yeditepe.firstspingproject.entity.Payment;
import com.yeditepe.firstspingproject.entity.Ticket;
import com.yeditepe.firstspingproject.entity.User;
import com.yeditepe.firstspingproject.repository.PaymentRepository;
import com.yeditepe.firstspingproject.repository.TicketRepository;
import com.yeditepe.firstspingproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("null")
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a new payment
    public Payment createPayment(Long ticketId, Long userId, String paymentMethod) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Bilet bulunamadı. ID: " + ticketId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı. ID: " + userId));

        // Generate unique transaction ID
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setTicket(ticket);
        payment.setUser(user);
        payment.setAmount(ticket.getPrice());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PENDING");
        payment.setPaymentMethod(paymentMethod);

        return paymentRepository.save(payment);
    }

    // Complete payment (change status to COMPLETED)
    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı. ID: " + paymentId));

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Bu ödeme zaten tamamlanmış.");
        }

        payment.setStatus("COMPLETED");
        return paymentRepository.save(payment);
    }

    // Cancel payment
    public Payment cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı. ID: " + paymentId));

        if ("COMPLETED".equals(payment.getStatus())) {
            throw new RuntimeException("Tamamlanmış ödeme iptal edilemez.");
        }

        payment.setStatus("CANCELLED");
        return paymentRepository.save(payment);
    }

    // Get payment by ID
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı. ID: " + paymentId));
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payments by user
    public List<Payment> getPaymentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı. ID: " + userId));
        return paymentRepository.findByUser(user);
    }

    // Get payments by status
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    // Get completed payments by user (for revenue tracking)
    public List<Payment> getCompletedPaymentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı. ID: " + userId));
        return paymentRepository.findByUserAndStatus(user, "COMPLETED");
    }

    // Get payment by transaction ID
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("İşlem bulunamadı. ID: " + transactionId));
    }

    // Get payments by ticket
    public List<Payment> getPaymentsByTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Bilet bulunamadı. ID: " + ticketId));
        return paymentRepository.findByTicket(ticket);
    }

    // Calculate total revenue (sum of completed payments)
    public Double getTotalRevenue() {
        List<Payment> completedPayments = getPaymentsByStatus("COMPLETED");
        return completedPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    // Calculate user spending (sum of completed payments for user)
    public Double getUserSpending(Long userId) {
        List<Payment> completedPayments = getCompletedPaymentsByUser(userId);
        return completedPayments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    // Count completed payments
    public Long countCompletedPayments() {
        return paymentRepository.findAll().stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
    }

    // Get payment status
    public String getPaymentStatus(Long paymentId) {
        return getPaymentById(paymentId).getStatus();
    }

    // Refund payment
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Ödeme bulunamadı. ID: " + paymentId));

        if (!("COMPLETED".equals(payment.getStatus()))) {
            throw new RuntimeException("Sadece tamamlanmış ödemeler iade edilebilir.");
        }

        payment.setStatus("REFUNDED");
        return paymentRepository.save(payment);
    }

    // Delete payment
    public void deletePayment(Long paymentId) {
        if (!paymentRepository.existsById(paymentId)) {
            throw new RuntimeException("Ödeme bulunamadı. ID: " + paymentId);
        }
        paymentRepository.deleteById(paymentId);
    }
}
