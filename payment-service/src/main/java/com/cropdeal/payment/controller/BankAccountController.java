package com.cropdeal.payment.controller;

import com.cropdeal.payment.dto.BankAccountRequest;
import com.cropdeal.payment.dto.BankAccountResponse;
import com.cropdeal.payment.service.BankAccountService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank-accounts")
public class BankAccountController {
    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<BankAccountResponse> list(Principal principal) {
        return service.list(principal.getName());
    }

    @PostMapping
    public ResponseEntity<BankAccountResponse> create(@Valid @RequestBody BankAccountRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public BankAccountResponse update(@PathVariable Long id, @Valid @RequestBody BankAccountRequest request,
            Principal principal) {
        return service.update(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        service.delete(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}