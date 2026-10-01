package com.cropdeal.payment.controller;

import com.cropdeal.payment.dto.CreatePaymentRequest;
import com.cropdeal.payment.dto.PaymentResponse;
import com.cropdeal.payment.dto.VerifyPaymentRequest;
import com.cropdeal.payment.entity.PaymentStatus;
import com.cropdeal.payment.entity.PaymentDocumentType;
import com.cropdeal.payment.entity.PaymentDocument;
import com.cropdeal.payment.service.PaymentDocumentService;
import com.cropdeal.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentDocumentService documentService;

    public PaymentController(PaymentService paymentService, PaymentDocumentService documentService) {
        this.paymentService = paymentService;
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.create(principal.getName(), request));
    }

    @PostMapping("/verify")
    public PaymentResponse verify(@Valid @RequestBody VerifyPaymentRequest request, Principal principal) {
        return paymentService.verify(principal.getName(), request);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestHeader("X-Razorpay-Signature") String signature,
            @RequestBody String payload) {
        paymentService.processWebhook(payload, signature);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public PaymentResponse get(@PathVariable Long id) {
        return paymentService.get(id);
    }

    @GetMapping("/order/{orderId}")
    public List<PaymentResponse> byOrder(@PathVariable Long orderId) {
        return paymentService.byOrder(orderId);
    }

    @PutMapping("/{id}/status")
    public PaymentResponse updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        return paymentService.updateStatus(id, status);
    }

    @GetMapping("/{paymentId}/invoice")
    public ResponseEntity<byte[]> invoice(@PathVariable Long paymentId, Principal principal) {
        return documentResponse(documentService.findForDealer(paymentId, PaymentDocumentType.INVOICE, principal.getName()));
    }

    @GetMapping("/{paymentId}/receipt")
    public ResponseEntity<byte[]> receipt(@PathVariable Long paymentId, Principal principal) {
        return documentResponse(documentService.findForDealer(paymentId, PaymentDocumentType.RECEIPT, principal.getName()));
    }

    private ResponseEntity<byte[]> documentResponse(PaymentDocument document) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.getDocumentNumber() + ".pdf\"")
                .body(document.getPdfData());
    }
}