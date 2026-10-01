package com.cropdeal.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cropdeal.payment.entity.Payout;
import com.cropdeal.payment.entity.PayoutStatus;
import com.cropdeal.payment.repository.BankAccountRepository;
import com.cropdeal.payment.repository.PayoutRepository;
import com.cropdeal.payment.service.PayoutService;
import com.razorpay.RazorpayClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PayoutAdminReadTest {
    @Mock PayoutRepository payoutRepository;
    @Mock BankAccountRepository bankAccountRepository;
    @Mock RazorpayClient razorpayClient;

    private PayoutService payoutService;

    @BeforeEach
    void setUp() {
        payoutService = new PayoutService(payoutRepository, bankAccountRepository, razorpayClient);
    }

    @Test
    void listsPayoutsFilteredByStatusWithoutExposingTransferId() {
        Payout payout = payout(PayoutStatus.PENDING);
        when(payoutRepository.findByStatus(eq(PayoutStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(payout)));

        var result = payoutService.adminPayouts(PayoutStatus.PENDING,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertEquals(1, result.getTotalElements());
        assertEquals(PayoutStatus.PENDING, result.getContent().get(0).status());
        verify(payoutRepository).findByStatus(eq(PayoutStatus.PENDING), any());
    }

    @Test
    void returnsNotFoundForUnknownPayout() {
        when(payoutRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> payoutService.adminPayout(999L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void repeatedExecutionOfProcessingPayoutReturnsExistingProcessingResult() {
        Payout payout = payout(PayoutStatus.PROCESSING);
        payout.setRazorpayTransferId("transfer-123");
        when(payoutRepository.findByIdForUpdate(13L)).thenReturn(Optional.of(payout));

        var result = payoutService.execute(13L);

        assertEquals(PayoutStatus.PROCESSING, result.status());
    }

    @Test
    void failedPayoutCannotBeExecutedAgainAutomatically() {
        Payout payout = payout(PayoutStatus.FAILED);
        when(payoutRepository.findByIdForUpdate(13L)).thenReturn(Optional.of(payout));

        Assertions.assertThrows(IllegalStateException.class, () -> payoutService.execute(13L));
    }

    private Payout payout(PayoutStatus status) {
        Payout payout = new Payout();
        payout.setOrderId(21L);
        payout.setFarmerId(7L);
        payout.setFarmerEmail("farmer@example.com");
        payout.setBankAccountId(4L);
        payout.setGrossAmount(new BigDecimal("100.00"));
        payout.setCommissionAmount(new BigDecimal("5.00"));
        payout.setFarmerAmount(new BigDecimal("95.00"));
        payout.setStatus(status);
        payout.setCreatedAt(LocalDateTime.now());
        payout.setRazorpayTransferId("transfer-secret-reference");
        return payout;
    }
}