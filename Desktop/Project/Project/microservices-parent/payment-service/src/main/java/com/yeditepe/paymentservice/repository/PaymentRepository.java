package com.yeditepe.paymentservice.repository;

import com.yeditepe.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserId(Long userId);

    List<Payment> findByStatus(String status);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByTicketId(Long ticketId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.userId = :userId AND p.status = 'COMPLETED'")
    Double getTotalSpendingByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'COMPLETED'")
    Double getTotalRevenue();

    @Query("SELECT p FROM Payment p WHERE p.status = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByStatusAndDateRange(
            @Param("status") String status,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );
}
