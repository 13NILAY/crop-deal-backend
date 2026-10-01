package com.cropdeal.payment.controller;

import com.cropdeal.payment.dto.CreatePayoutRequest;
import com.cropdeal.payment.dto.PayoutResponse;
import com.cropdeal.payment.entity.PayoutStatus;
import com.cropdeal.payment.service.PayoutService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import com.cropdeal.payment.service.AdminActivityClient;

@RestController
@RequestMapping("/payouts")
public class PayoutController {
    private final PayoutService service;
    private final AdminActivityClient activityClient;
    public PayoutController(PayoutService service, AdminActivityClient activityClient) {
        this.service = service;
        this.activityClient = activityClient;
    }
    @GetMapping @PreAuthorize("hasRole('ADMIN')")
    public Page<PayoutResponse> all(@RequestParam(required = false) PayoutStatus status,
                                    @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.adminPayouts(status, pageable);
    }
    @GetMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public PayoutResponse get(@PathVariable Long id) { return service.adminPayout(id); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public PayoutResponse create(@Valid @RequestBody CreatePayoutRequest request,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        PayoutResponse payout = service.create(request);
        activityClient.record(authorization, "ADMIN_PAYOUT_CREATED", payout.id());
        return payout;
    }
    @GetMapping("/farmer/me") public List<PayoutResponse> mine(Principal principal) { return service.byFarmer(principal.getName()); }
    @PutMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN')")
    public PayoutResponse status(@PathVariable Long id, @RequestParam PayoutStatus status,
                                 @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        PayoutResponse payout = service.updateStatus(id, status);
        activityClient.record(authorization, "ADMIN_PAYOUT_STATUS_UPDATED", id);
        return payout;
    }
    @PostMapping("/{id}/execute") @PreAuthorize("hasRole('ADMIN')")
    public PayoutResponse execute(@PathVariable Long id,
                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        PayoutResponse payout = service.execute(id);
        activityClient.record(authorization, "ADMIN_PAYOUT_EXECUTED", id);
        return payout;
    }
}