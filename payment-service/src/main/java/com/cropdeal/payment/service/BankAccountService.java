package com.cropdeal.payment.service;

import com.cropdeal.payment.config.BankAccountCrypto;
import com.cropdeal.payment.dto.BankAccountRequest;
import com.cropdeal.payment.dto.BankAccountResponse;
import com.cropdeal.payment.entity.BankAccount;
import com.cropdeal.payment.repository.BankAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BankAccountService {
    private final BankAccountRepository repository;
    private final BankAccountCrypto crypto;
    public BankAccountService(BankAccountRepository repository, BankAccountCrypto crypto) { this.repository = repository; this.crypto = crypto; }
    public List<BankAccountResponse> list(String userEmail) { return repository.findByUserEmailOrderByDefaultAccountDescIdAsc(userEmail).stream().map(this::response).toList(); }
    public BankAccountResponse create(String userEmail, BankAccountRequest request) {
        if (request.defaultAccount() || repository.findByUserEmailOrderByDefaultAccountDescIdAsc(userEmail).isEmpty()) clearDefault(userEmail);
        BankAccount account = new BankAccount();
        account.setUserEmail(userEmail);
        apply(account, request);
        account.setVerificationStatus("UNVERIFIED");
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return response(repository.save(account));
    }
    public BankAccountResponse update(String userEmail, Long id, BankAccountRequest request) {
        BankAccount account = repository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
        if (request.defaultAccount()) clearDefault(userEmail);
        apply(account, request);
        account.setUpdatedAt(LocalDateTime.now());
        return response(repository.save(account));
    }
    public void delete(String userEmail, Long id) {
        BankAccount account = repository.findByIdAndUserEmail(id, userEmail).orElseThrow(() -> new IllegalArgumentException("Bank account not found"));
        repository.delete(account);
    }
    private void clearDefault(String userEmail) {
        var accounts = repository.findByUserEmailOrderByDefaultAccountDescIdAsc(userEmail);
        accounts.forEach(account -> account.setDefaultAccount(false));
        repository.saveAll(accounts);
    }
    private void apply(BankAccount account, BankAccountRequest request) { account.setAccountHolderName(request.accountHolderName()); account.setEncryptedAccountNumber(crypto.encrypt(request.accountNumber())); account.setEncryptedIfscCode(crypto.encrypt(request.ifscCode().toUpperCase())); account.setBankName(request.bankName()); account.setDefaultAccount(request.defaultAccount()); account.setRazorpayLinkedAccountId(request.razorpayLinkedAccountId()); }
    private BankAccountResponse response(BankAccount account) { String number = crypto.decrypt(account.getEncryptedAccountNumber()); String ifsc = crypto.decrypt(account.getEncryptedIfscCode()); return new BankAccountResponse(account.getId(), account.getAccountHolderName(), mask(number), mask(ifsc), account.getBankName(), account.isDefaultAccount(), account.getVerificationStatus()); }
    private String mask(String value) { return "*".repeat(Math.max(0, value.length() - 4)) + value.substring(Math.max(0, value.length() - 4)); }
}