package com.cropdeal.payment.repository;

import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByOrderId(Long orderId);
    org.springframework.data.domain.Page<Payment> findByStatus(PaymentStatus status,
            org.springframework.data.domain.Pageable pageable);
    long countByStatus(PaymentStatus status);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
    Optional<Payment> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);
}