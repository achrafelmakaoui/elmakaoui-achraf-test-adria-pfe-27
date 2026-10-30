package com.adria.bank.account.service;

import com.adria.bank.account.domain.Account;
import com.adria.bank.account.domain.AccountStatus;
import com.adria.bank.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account create(String customerReference, String currency, double initialBalance) {
        if (customerReference == null || customerReference.isBlank()) {
            throw new BusinessRuleException("customerReference is required");
        }
        if (currency == null || !currency.matches("[A-Za-z]{3}")) {
            throw new BusinessRuleException("currency must be a 3-letter code (e.g. EUR, MAD)");
        }
        if (initialBalance < 0) {
            throw new BusinessRuleException("initial balance cannot be negative");
        }
        return repository.save(new Account(customerReference.trim(), currency.toUpperCase(), initialBalance));
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Account findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account " + id + " not found"));
    }

    public Account updateStatus(UUID id, AccountStatus status) {
        Account account = findById(id);
        account.setStatus(status);
        return repository.save(account);
    }

    public Account debit(UUID id, double amount) {
        checkAmount(amount);
        Account account = findById(id);
        checkActive(account);
        if (account.getBalance() < amount) {
            throw new BusinessRuleException("Insufficient balance on account " + id);
        }
        account.setBalance(account.getBalance() - amount);
        return repository.save(account);
    }

    public Account credit(UUID id, double amount) {
        checkAmount(amount);
        Account account = findById(id);
        checkActive(account);
        account.setBalance(account.getBalance() + amount);
        return repository.save(account);
    }

    private void checkAmount(double amount) {
        if (!(amount > 0)) {
            throw new BusinessRuleException("amount must be strictly positive");
        }
    }

    private void checkActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Account " + account.getId() + " is " + account.getStatus());
        }
    }
}
