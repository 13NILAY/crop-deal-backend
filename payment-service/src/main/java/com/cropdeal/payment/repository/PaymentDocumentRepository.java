package com.cropdeal.payment.repository;

import com.cropdeal.payment.entity.PaymentDocument;
import com.cropdeal.payment.entity.PaymentDocumentType;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentDocumentRepository extends JpaRepository<PaymentDocument, Long> {
    Optional<PaymentDocument> findByPaymentIdAndType(Long paymentId, PaymentDocumentType type);
    Optional<PaymentDocument> findByPaymentIdAndTypeAndDealerEmail(Long paymentId, PaymentDocumentType type,
                                                                    String dealerEmail);
    List<PaymentDocument> findByPaymentId(Long paymentId);
}