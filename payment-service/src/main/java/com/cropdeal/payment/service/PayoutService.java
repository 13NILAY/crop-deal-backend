package com.cropdeal.payment.service;

import com.cropdeal.payment.dto.CreatePayoutRequest;
import com.cropdeal.payment.dto.PayoutResponse;
import com.cropdeal.payment.entity.Payout;
import com.cropdeal.payment.entity.PayoutStatus;
import com.cropdeal.payment.repository.PayoutRepository;
import com.cropdeal.payment.repository.BankAccountRepository;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PayoutService {
    private final PayoutRepository repository;
    private final BankAccountRepository bankAccountRepository;
    private final RazorpayClient razorpayClient;
    @org.springframework.beans.factory.annotation.Value("${razorpay.key-id:}") private String razorpayKeyId;
    @org.springframework.beans.factory.annotation.Value("${razorpay.key-secret:}") private String razorpayKeySecret;
    public PayoutService(PayoutRepository repository, BankAccountRepository bankAccountRepository, RazorpayClient razorpayClient) { this.repository = repository; this.bankAccountRepository = bankAccountRepository; this.razorpayClient = razorpayClient; }
    public PayoutResponse create(CreatePayoutRequest request) {
        if (request.commissionAmount().compareTo(request.grossAmount()) >= 0) throw new IllegalArgumentException("Commission must be less than gross amount");
        Payout payout = new Payout();
        payout.setOrderId(request.orderId()); payout.setFarmerId(request.farmerId()); payout.setFarmerEmail(request.farmerEmail());
        payout.setBankAccountId(request.bankAccountId()); payout.setGrossAmount(request.grossAmount());
        payout.setCommissionAmount(request.commissionAmount()); payout.setFarmerAmount(request.grossAmount().subtract(request.commissionAmount()));
        payout.setStatus(PayoutStatus.PENDING); payout.setCreatedAt(LocalDateTime.now());
        return response(repository.save(payout));
    }
    public List<PayoutResponse> byFarmer(String email) { return repository.findByFarmerEmailOrderByCreatedAtDesc(email).stream().map(this::response).toList(); }
        public Page<PayoutResponse> adminPayouts(PayoutStatus status, Pageable pageable) {
        Pageable boundedPageable = PageRequest.of(pageable.getPageNumber(),
            Math.max(1, Math.min(pageable.getPageSize(), 100)), pageable.getSort());
        Page<Payout> payouts = status == null
            ? repository.findAll(boundedPageable)
            : repository.findByStatus(status, boundedPageable);
        return payouts.map(this::response);
        }
        public PayoutResponse adminPayout(Long id) {
        return repository.findById(id).map(this::response)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout not found"));
        }
    public PayoutResponse updateStatus(Long id, PayoutStatus status) { Payout payout = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Payout not found")); payout.setStatus(status); return response(repository.save(payout)); }
    @Transactional
    public PayoutResponse execute(Long id) {
        Payout payout = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Payout not found"));
        if (payout.getStatus() == PayoutStatus.PROCESSING || payout.getStatus() == PayoutStatus.PAID) {
            return response(payout);
        }
        if (payout.getStatus() != PayoutStatus.PENDING || payout.getRazorpayTransferId() != null) {
            throw new IllegalStateException("Only pending payouts without an existing transfer can be executed");
        }
        if (razorpayKeyId.isBlank() || razorpayKeySecret.isBlank()) throw new IllegalStateException("Razorpay credentials are not configured");
        var account = bankAccountRepository.findById(payout.getBankAccountId()).orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
        if (!"VERIFIED".equals(account.getVerificationStatus()) || account.getRazorpayLinkedAccountId() == null || account.getRazorpayLinkedAccountId().isBlank()) throw new IllegalStateException("Verified Razorpay linked account is required");
        payout.setStatus(PayoutStatus.PROCESSING);
        repository.saveAndFlush(payout);
        try {
            JSONObject transfer = new JSONObject();
            transfer.put("account", account.getRazorpayLinkedAccountId());
            transfer.put("amount", payout.getFarmerAmount().movePointRight(2).longValueExact());
            transfer.put("currency", "INR");
            transfer.put("notes", new JSONObject().put("orderId", payout.getOrderId()).put("farmerId", payout.getFarmerId()));
            String transferId = razorpayClient.transfers.create(transfer).get("id");
            payout.setRazorpayTransferId(transferId);
            return response(repository.save(payout));
        } catch (Exception ex) { payout.setStatus(PayoutStatus.FAILED); repository.save(payout); throw new IllegalStateException("Razorpay payout failed", ex); }
    }
    private PayoutResponse response(Payout p) { return new PayoutResponse(p.getId(), p.getOrderId(), p.getFarmerId(), p.getFarmerEmail(), p.getBankAccountId(), p.getGrossAmount(), p.getCommissionAmount(), p.getFarmerAmount(), p.getStatus(), p.getCreatedAt()); }
}