package com.yeditepe.firstspingproject.repository;

import com.yeditepe.firstspingproject.entity.Payment;
import com.yeditepe.firstspingproject.entity.Ticket;
import com.yeditepe.firstspingproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payments by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    // Find payments by user
    List<Payment> findByUser(User user);

    // Find payments by ticket
    List<Payment> findByTicket(Ticket ticket);

    // Find payments by status
    List<Payment> findByStatus(String status);

    // Find payments by payment method
    List<Payment> findByPaymentMethod(String paymentMethod);

    // Find completed payments for a user
    List<Payment> findByUserAndStatus(User user, String status);

    // Custom query - find completed payments for a user
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsForUser(@Param("userId") Long userId);
}
