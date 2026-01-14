package com.yeditepe.firstspingproject.controllers;

import com.yeditepe.firstspingproject.entity.Payment;
import com.yeditepe.firstspingproject.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Welcome endpoint
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to Payment Service!";
    }

    // Create payment
    @PostMapping("/create")
    public ResponseEntity<Payment> createPayment(
            @RequestParam Long ticketId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "CREDIT_CARD") String paymentMethod) {
        try {
            Payment payment = paymentService.createPayment(ticketId, userId, paymentMethod);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Complete payment
    @PutMapping("/{id}/complete")
    public ResponseEntity<Payment> completePayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.completePayment(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Cancel payment
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Payment> cancelPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.cancelPayment(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Refund payment
    @PutMapping("/{id}/refund")
    public ResponseEntity<Payment> refundPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.refundPayment(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get all payments
    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    // Get payments by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUser(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByUser(userId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get payments by status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    // Get payment by transaction ID
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get payment status
    @GetMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> getPaymentStatus(@PathVariable Long id) {
        try {
            String status = paymentService.getPaymentStatus(id);
            Map<String, String> response = new HashMap<>();
            response.put("paymentId", id.toString());
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get user spending
    @GetMapping("/user/{userId}/spending")
    public ResponseEntity<Map<String, Object>> getUserSpending(@PathVariable Long userId) {
        try {
            Double spending = paymentService.getUserSpending(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalSpending", spending);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Get total revenue
    @GetMapping("/revenue/total")
    public ResponseEntity<Map<String, Object>> getTotalRevenue() {
        Double revenue = paymentService.getTotalRevenue();
        Long count = paymentService.countCompletedPayments();
        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", revenue);
        response.put("completedPayments", count);
        return ResponseEntity.ok(response);
    }

    // Get completed payments by user
    @GetMapping("/user/{userId}/completed")
    public ResponseEntity<List<Payment>> getCompletedPaymentsByUser(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getCompletedPaymentsByUser(userId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete payment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
