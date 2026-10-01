package com.cropdeal.payment.repository;

import com.cropdeal.payment.entity.Payout;
import com.cropdeal.payment.entity.PayoutStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    List<Payout> findByFarmerEmailOrderByCreatedAtDesc(String farmerEmail);
    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);
    long countByStatus(PayoutStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payout from Payout payout where payout.id = :id")
    Optional<Payout> findByIdForUpdate(@Param("id") Long id);
}