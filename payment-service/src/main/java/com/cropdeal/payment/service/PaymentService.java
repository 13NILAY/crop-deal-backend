package com.cropdeal.payment.service;

import com.cropdeal.payment.dto.CreatePaymentRequest;
import com.cropdeal.payment.dto.PaymentResponse;
import com.cropdeal.payment.dto.AdminPaymentResponse;
import com.cropdeal.payment.dto.VerifyPaymentRequest;
import com.cropdeal.payment.entity.Payment;
import com.cropdeal.payment.entity.PaymentStatus;
import com.cropdeal.payment.exception.PaymentProviderNotConfiguredException;
import com.cropdeal.payment.exception.PaymentNotFoundException;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import java.math.RoundingMode;
import org.json.JSONObject;
import com.cropdeal.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    private final PaymentDocumentService documentService;
    private final PaymentEmailService emailService;
    private final RazorpayClient razorpayClient;
    @org.springframework.beans.factory.annotation.Value("${razorpay.key-id:}")
    private String razorpayKeyId;
    @org.springframework.beans.factory.annotation.Value("${razorpay.key-secret:}")
    private String razorpayKeySecret;
    @org.springframework.beans.factory.annotation.Value("${razorpay.webhook-secret:}")
    private String razorpayWebhookSecret;
    @org.springframework.beans.factory.annotation.Value("${payment.manual-status-enabled:false}")
    private boolean manualStatusEnabled;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventPublisher eventPublisher,
                          RazorpayClient razorpayClient, PaymentDocumentService documentService,
                          PaymentEmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.razorpayClient = razorpayClient;
        this.documentService = documentService;
        this.emailService = emailService;
    }
    @Transactional
    public PaymentResponse create(String dealerEmail, CreatePaymentRequest request) {
        requireRazorpayCredentials();
        long amountInPaise = request.amount().movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        String receipt = "cropdeal-order-" + request.orderId() + "-" + System.currentTimeMillis();
        JSONObject razorpayOrderRequest = new JSONObject();
        razorpayOrderRequest.put("amount", amountInPaise);
        razorpayOrderRequest.put("currency", "INR");
        razorpayOrderRequest.put("receipt", receipt);
        razorpayOrderRequest.put("payment_capture", 1);
        String razorpayOrderId;
        try {
            razorpayOrderId = razorpayClient.orders.create(razorpayOrderRequest).get("id");
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create Razorpay order", ex);
        }
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setRazorpayOrderId(razorpayOrderId);
        payment.setDealerEmail(dealerEmail);
        payment.setAmount(request.amount());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        publish(saved);
        return toResponse(saved);
    }
    public PaymentResponse get(Long id) { return toResponse(find(id)); }
    public Page<AdminPaymentResponse> adminSearch(PaymentStatus status, Long orderId, String search,
                                                  Pageable pageable) {
        Specification<Payment> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (status != null) predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (orderId != null) predicates.add(criteriaBuilder.equal(root.get("orderId"), orderId));
            if (search != null && !search.isBlank()) {
                String term = "%" + search.trim().toLowerCase(java.util.Locale.ROOT) + "%";
                var searchPredicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
                searchPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("dealerEmail")), term));
                try {
                    Long paymentId = Long.valueOf(search.trim());
                    searchPredicates.add(criteriaBuilder.equal(root.get("id"), paymentId));
                } catch (NumberFormatException ignored) {
                    // Non-numeric searches match dealer email only.
                }
                predicates.add(criteriaBuilder.or(searchPredicates.toArray(jakarta.persistence.criteria.Predicate[]::new)));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Pageable boundedPageable = PageRequest.of(Math.max(0, pageable.getPageNumber()),
                Math.max(1, Math.min(pageable.getPageSize(), 100)), pageable.getSort());
        return paymentRepository.findAll(specification, boundedPageable).map(AdminPaymentResponse::from);
    }
    public AdminPaymentResponse adminGet(Long id) {
        return paymentRepository.findById(id).map(AdminPaymentResponse::from)
                .orElseThrow(() -> new PaymentNotFoundException(id));
    }
    public List<PaymentResponse> byOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
    }
    @Transactional
    public PaymentResponse updateStatus(Long id, PaymentStatus status) {
        if (status == PaymentStatus.PENDING || (status == PaymentStatus.SUCCESS && !manualStatusEnabled)) {
            throw new IllegalArgumentException("Use Razorpay verification or webhook processing for this status");
        }
        Payment payment = find(id);
        payment.setStatus(status);
        if (status == PaymentStatus.SUCCESS) {
            documentService.createForSuccessfulPayment(payment);
            emailService.sendPaymentDocuments(payment);
        }
        Payment saved = paymentRepository.save(payment);
        publish(saved);
        return toResponse(saved);
    }
    @Transactional
    public PaymentResponse verify(String dealerEmail, VerifyPaymentRequest request) {
        requireRazorpayCredentials();
        Payment payment = paymentRepository.findByRazorpayOrderId(request.razorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(request.razorpayOrderId()));
        if (!payment.getDealerEmail().equals(dealerEmail)) {
            throw new org.springframework.security.access.AccessDeniedException("Payment belongs to another dealer");
        }
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", request.razorpayOrderId());
            attributes.put("razorpay_payment_id", request.razorpayPaymentId());
            attributes.put("razorpay_signature", request.razorpaySignature());
            if (!Utils.verifyPaymentSignature(attributes, razorpayKeySecret)) {
                throw new IllegalArgumentException("Invalid Razorpay payment signature");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Razorpay payment verification failed", ex);
        }
        payment.setRazorpayPaymentId(request.razorpayPaymentId());
        payment.setStatus(PaymentStatus.SUCCESS);
        Payment saved = paymentRepository.save(payment);
        documentService.createForSuccessfulPayment(saved);
        emailService.sendPaymentDocuments(saved);
        publish(saved);
        return toResponse(saved);
    }
    @Transactional
    public void processWebhook(String payload, String signature) {
        if (razorpayWebhookSecret.isBlank()) {
            throw new PaymentProviderNotConfiguredException();
        }
        try {
            if (!Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret)) {
                throw new IllegalArgumentException("Invalid Razorpay webhook signature");
            }
            JSONObject webhook = new JSONObject(payload);
            JSONObject paymentEntity = webhook.getJSONObject("payload")
                    .getJSONObject("payment").getJSONObject("entity");
            String razorpayOrderId = paymentEntity.optString("order_id");
            Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId).orElse(null);
            if (payment == null) {
                return;
            }
            String event = webhook.optString("event");
            if (!"payment.captured".equals(event) && !"payment.failed".equals(event)) {
                return;
            }
            if ("payment.captured".equals(event)) {
                payment.setRazorpayPaymentId(paymentEntity.optString("id"));
                payment.setStatus(PaymentStatus.SUCCESS);
            } else if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.FAILED);
            }
            Payment saved = paymentRepository.save(payment);
            if (saved.getStatus() == PaymentStatus.SUCCESS) {
                documentService.createForSuccessfulPayment(saved);
                emailService.sendPaymentDocuments(saved);
            }
            publish(saved);
        } catch (PaymentProviderNotConfiguredException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Razorpay webhook processing failed", ex);
        }
    }
    private void requireRazorpayCredentials() {
        if (razorpayKeyId.isBlank() || razorpayKeySecret.isBlank()) {
            throw new PaymentProviderNotConfiguredException();
        }
    }
    private void publish(Payment payment) {
        eventPublisher.publish(new com.cropdeal.payment.event.PaymentStatusChangedEvent(payment.getId(),
                payment.getOrderId(), payment.getDealerEmail(), payment.getAmount(), payment.getStatus()));
    }
    private Payment find(Long id) { return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id)); }
    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(payment.getId(), payment.getOrderId(), payment.getDealerEmail(), payment.getAmount(),
            payment.getStatus(), payment.getRazorpayOrderId(), payment.getRazorpayPaymentId(), payment.getCreatedAt());
    }
}