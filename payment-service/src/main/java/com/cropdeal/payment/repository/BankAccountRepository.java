package com.cropdeal.payment.repository;

import com.cropdeal.payment.entity.BankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findByUserEmailOrderByDefaultAccountDescIdAsc(String userEmail);
    Optional<BankAccount> findByIdAndUserEmail(Long id, String userEmail);
}